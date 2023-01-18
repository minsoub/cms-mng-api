package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.*
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisBanner
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeService(
    private val noticeRepository: CmsNoticeRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository,
    private val awsProperties: AwsProperties
) : CmsBaseService() {

    /**
     * 공지사항 생성
     * @param request 공지사항 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     * @param account 생성자 정보
     */
    @Transactional
    suspend fun createNotice(
        request: NoticeRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<NoticeDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && request.validateNotice() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = noticeRepository
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

                val entity: CmsNotice = request.toEntity()

                if (checkRequestCondition(request)) {
                    findAndDeleteAllBanner(account)
                    addBannerToRedis(entity.toRedisEntity())
                }

                noticeRepository.save(entity).toResponse(awsProperties.kmsKey).also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                    applyTopToRedis(5)
                }
                noticeRepository.findById(entity.id)?.toResponse(awsProperties.kmsKey)
            }
        }
    )

    suspend fun getNotices(searchParams: SearchParams, account: Account): Result<ListResponse<NoticeResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<NoticeResponse>> = async {
                noticeRepository.findByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                )
                    .map { it.toDraftResponse() }
                    .toList()
            }

            val notices: Deferred<List<NoticeResponse>> = async {
                noticeRepository.findByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            val top: Deferred<List<NoticeResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                noticeRepository.findByCriteria(criteria = criteria.withoutDraft(), pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(notices.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(notices.await().size.toLong())
            )
        }
    }

    suspend fun getNotice(id: String): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    @Transactional
    suspend fun updateNotice(
        id: String,
        request: NoticeRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<NoticeDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && request.validateNotice() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = noticeRepository
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

                noticeRepository.findById(id)?.let { notice ->
                    if (notice.isBanner != request.isBanner) {
                        when (request.isBanner) {
                            true -> {
                                findAndDeleteAllBanner(account)
                                addBannerToRedis(notice.toRedisEntity())
                            }

                            false -> {
                                deleteBannerFromRedis()
                            }
                        }
                    }
                    notice.setUpdateInfo(
                        password = awsProperties.kmsKey,
                        saltKey = awsProperties.saltKey,
                        ivKey = awsProperties.ivKey,
                        request = request,
                        account = account,
                        fileRequest = fileRequest
                    )
                    noticeRepository.save(notice).toResponse(awsProperties.kmsKey).also {
                        if (needUpdate(notice, request)) {
                            applyToRedis()
                        }
                        applyTopToRedis(5)
                    }
                }
            }
        }
    )

    private fun needUpdate(
        it: CmsNotice,
        request: NoticeRequest
    ) = it.isFixTop != request.isFixTop || it.isDelete != request.isDelete || it.isShow != request.isShow ||
        it.isSchedule != request.isSchedule || it.title != request.title

    @Transactional
    suspend fun deleteNotice(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
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
                    noticeRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                        applyTopToRedis(5)
                    }
                }
            }
        }
    }

    @Transactional
    suspend fun createNoticeBanner(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
            it.isBanner = true
            it.setUpdateInfo(
                password = awsProperties.kmsKey,
                saltKey = awsProperties.saltKey,
                ivKey = awsProperties.ivKey,
                account = account
            )
            findAndDeleteAllBanner(account)
            noticeRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                addBannerToRedis(response.toRedisBannerEntity())
            }
        }
    }

    @Transactional
    suspend fun deleteNoticeBanner(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
            it.isBanner = false
            it.setUpdateInfo(
                password = awsProperties.kmsKey,
                saltKey = awsProperties.saltKey,
                ivKey = awsProperties.ivKey,
                account = account
            )
            noticeRepository.save(it).toResponse(awsProperties.kmsKey).also { deleteBannerFromRedis() }
        }
    }

    private fun checkRequestCondition(request: NoticeRequest): Boolean =
        request.isBanner && !request.isDelete && request.isShow && !request.isDraft

    private suspend fun applyToRedis() {
        noticeRepository.getFixItems().map { item -> item.toRedisEntity() }.also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_NOTICE_FIX,
                value = totalList.toList(),
                typeReference = object : TypeReference<List<RedisBanner>>() {}
            )
        }.collect()
    }

    private suspend fun applyTopToRedis(limit: Int) {
        noticeRepository.findByIsShowIsTrueAndIsDeleteIsFalseAndIsDraftIsFalseAndIsScheduleIsFalseOrderByScreenDateDesc(PageRequest.of(0, limit))
            .map { it.toRedisEntity() }
            .also { topList ->
                redisRepository.addOrUpdateRBucket(
                    bucketKey = CMS_NOTICE_RECENT,
                    value = topList.toList(),
                    typeReference = object : TypeReference<List<RedisBanner>>() {}
                )
            }.collect()
    }

    private suspend fun addBannerToRedis(redisEntity: RedisBanner) {
        redisRepository.addOrUpdateRBucket(
            bucketKey = CMS_NOTICE_BANNER,
            value = redisEntity,
            typeReference = object : TypeReference<RedisBanner>() {}
        )
    }

    private suspend fun deleteBannerFromRedis() {
        redisRepository.deleteRBucket(
            bucketKey = CMS_NOTICE_BANNER,
            typeReference = object : TypeReference<RedisBanner>() {}
        )
    }

    private suspend fun findAndDeleteAllBanner(account: Account) {
        noticeRepository.findByIsBannerIsTrueAndIsDraftIsFalseAndIsScheduleIsFalse().map { notice ->
            notice.isBanner = false
            notice.setUpdateInfo(
                password = awsProperties.kmsKey,
                saltKey = awsProperties.saltKey,
                ivKey = awsProperties.ivKey,
                account = account
            )
            return@map notice
        }.toList().also { noticeList ->
            noticeRepository.saveAll(noticeList).collect()
        }
    }
}
