package com.bithumbsystems.cms.api.service

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.exception.CustomException
import com.bithumbsystems.cms.api.model.enums.ActionType
import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_EVENT_FIX
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsEventParticipantsRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsEventRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisBoard
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.Result
import com.google.gson.Gson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class EventService(
    private val eventRepository: CmsEventRepository,
    private val eventParticipantsRepository: CmsEventParticipantsRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository,
    private val amazonSQS: AmazonSQSAsync,
    private val awsProperties: AwsProperties,
    private val webClient: WebClient,
) : CmsBaseService() {

    /**
     * 이벤트 생성
     * @param request 이벤트 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     * @param account 생성자 정보
     */
    @Transactional
    suspend fun createEvent(
        request: EventRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EventDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && request.validateEvent() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = eventRepository
            )
        },
        action = {
            coroutineScope {
                fileRequest?.let {
                    it.setKeys().also {
                        launch {
                            fileService.addFileInfo(fileRequest = fileRequest, account = account)
                        }
                        request.setCreateInfo(
                            password = awsProperties.kmsKey,
                            saltKey = awsProperties.saltKey,
                            ivKey = awsProperties.ivKey,
                            fileRequest = fileRequest,
                            account = account
                        )
                    }
                } ?: request.setCreateInfo(
                    password = awsProperties.kmsKey,
                    saltKey = awsProperties.saltKey,
                    ivKey = awsProperties.ivKey,
                    account = account
                )

                eventRepository.save(request.toEntity()).toResponse(awsProperties.kmsKey).also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    suspend fun getEvents(searchParams: SearchParams, account: Account): Result<ListResponse<EventResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<EventResponse>> = async {
                eventRepository.findByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                )
                    .map { it.toDraftResponse() }
                    .toList()
            }

            val events: Deferred<List<EventResponse>> = async {
                eventRepository.findByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            val top: Deferred<List<EventResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                eventRepository.findByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(events.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(events.await().size.toLong())
            )
        }
    }

    suspend fun getEvent(id: String): Result<EventDetailResponse?, ErrorData> = executeIn {
        eventRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    @Transactional
    suspend fun updateEvent(
        id: String,
        request: EventRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EventDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && request.validateEvent() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = eventRepository
            )
        },
        action = {
            coroutineScope {
                fileRequest?.let {
                    it.setKeys().also {
                        launch {
                            fileService.addFileInfo(fileRequest = fileRequest, account = account)
                        }
                    }
                }

                eventRepository.findById(id)?.let {
                    val isChange: Boolean =
                        it.isFixTop != request.isFixTop || it.isDelete != request.isDelete || it.isShow != request.isShow ||
                            it.isSchedule != request.isSchedule || it.title != request.title
                    it.setUpdateInfo(
                        password = awsProperties.kmsKey,
                        saltKey = awsProperties.saltKey,
                        ivKey = awsProperties.ivKey,
                        request = request,
                        account = account,
                        fileRequest = fileRequest
                    )
                    eventRepository.save(it).toResponse(awsProperties.kmsKey).also {
                        if (isChange) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deleteEvent(id: String, account: Account): Result<EventDetailResponse?, ErrorData> = executeIn {
        eventRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse(awsProperties.kmsKey)
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(
                        password = awsProperties.kmsKey,
                        saltKey = awsProperties.saltKey,
                        ivKey = awsProperties.ivKey,
                        account = account
                    )
                    eventRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }

    fun downloadEventExcel(eventId: String, reason: String, account: Account): Mono<ByteArrayInputStream> =
        mono {
            val eventParticipants: List<EventParticipantsResponse> =
                eventParticipantsRepository.findByEventId(eventId).map { it.toResponse(awsProperties.kmsKey) }.toList()

            eventParticipantsRepository.findByEventId(eventId).map {
                it.uid = decryptUid(it.uid) // todo 복호화 처리
                it.toResponse(awsProperties.kmsKey)
            }.toList()

            val description = "이벤트 참여자 엑셀 다운로드"
            logger.info("$eventId, $description, $reason")

            sendReason(reason = reason, description = description, account = account)
            createExcelFile(eventParticipants).awaitSingleOrNull()
        }

    private suspend fun decryptUid(uidToken: String): String {
        val result: BithumbResponse<UidTokenResponse> =
            webClient.get().uri { uriBuilder -> uriBuilder.path("/v1/meminfo/uid").queryParam("uidToken", uidToken).build() }
                .accept(APPLICATION_JSON).retrieve().awaitBody()
        return result.data?.uid ?: throw CustomException(ErrorCode.UID_DECRYPT_FAIL, ErrorCode.UID_DECRYPT_FAIL.message)
    } // todo

    private suspend fun applyToRedis() {
        eventRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_EVENT_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
    }

    private fun sendReason(reason: String, description: String, account: Account) {
        val eventReasonRequest = EventReasonRequest(
            accountId = account.accountId,
            actionType = ActionType.DOWNLOAD,
            reason = reason,
            email = account.email,
            description = description,
            siteId = account.mySiteId,
            ip = account.userIp
        )

        amazonSQS.sendMessage(
            SendMessageRequest(
                "${awsProperties.sqsEndPoint.trim()}/${awsProperties.sqsPrivacyReasonQueueName.trim()}",
                Gson().toJson(eventReasonRequest)
            ).withMessageGroupId(eventReasonRequest.siteId).withMessageDeduplicationId(UUID.randomUUID().toString())
        )
    }

    private fun createExcelFile(list: List<EventParticipantsResponse>): Mono<ByteArrayInputStream> {
        return Mono.fromCallable {
            logger.debug("엑셀 파일 생성 시작")
            val workbook =
                SXSSFWorkbook(SXSSFWorkbook.DEFAULT_WINDOW_SIZE) // keep 100 rows in memory, exceeding rows will be flushed to disk
            val out = ByteArrayOutputStream()
            val sheet: Sheet = workbook.createSheet("UID 목록")
            val headerFont: Font = workbook.createFont().apply {
                this.fontName = "맑은 고딕"
                this.fontHeight = (10 * 20).toShort()
                this.bold = true
                this.color = IndexedColors.BLACK.index
            }

            // Header Cell 스타일 생성
            val headerStyle: CellStyle = workbook.createCellStyle().apply {
                this.alignment = HorizontalAlignment.CENTER
                this.verticalAlignment = VerticalAlignment.CENTER
                this.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                this.fillPattern = FillPatternType.SOLID_FOREGROUND
                this.setFont(headerFont)
            }

            // Row for Header
            val headerRow: Row = sheet.createRow(0)

            // Header
            val fields: Array<String> = arrayOf("번호", "UID", "참여일시", "개인정보 수집 및 이용 동의")
            for (col: Int in fields.indices) {
                headerRow.createCell(col).apply {
                    this.setCellValue(fields[col])
                    this.cellStyle = headerStyle
                }
            }

            // Body
            var rowIdx = 1
            for (res: EventParticipantsResponse in list) {
                sheet.createRow(rowIdx++).apply {
                    this.createCell(0).setCellValue((rowIdx - 1).toString())
                    this.createCell(1).setCellValue(res.uid)
                    this.createCell(2).setCellValue(res.createDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    res.isAgree?.let {
                        this.createCell(3).setCellValue(
                            when (it) {
                                true -> "Y"
                                false -> "N"
                            }
                        )
                    }
                }
            }
            workbook.write(out)
            logger.debug("엑셀 파일 생성 종료")
            ByteArrayInputStream(out.toByteArray())
        }.log()
    }
}
