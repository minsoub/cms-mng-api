package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.*
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.Logger
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisNotice
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
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeService(
    private val noticeRepository: CmsNoticeRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {
    val logger by Logger()

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
            request.validate() && request.validateNotice() && fileRequest?.validate() == true
        },
        action = {
            coroutineScope {
                fileRequest?.let {
                    it.setKeys().also {
                        launch {
                            fileService.addFileInfo(fileRequest = fileRequest, account = account)
                        }
                        request.setCreateInfo(fileRequest = fileRequest, account = account)
                    }
                } ?: request.setCreateInfo(account)

                val entity: CmsNotice = request.toEntity()

                if (request.isBanner) {
                    // 이전 게시글 있는지 여부 확인
                    findAndDeleteAllBanner(account)
                    addBannerToRedis(entity.toRedisEntity())
                }

                noticeRepository.save(entity).toResponse().also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                    applyTop5ToRedis()
                }
                noticeRepository.findById(entity.id)?.toResponse()
            }
        }
    )

    private suspend fun applyToRedis() {
        noticeRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_NOTICE_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisNotice>>() {}
            )
        }
    }

    private suspend fun applyTop5ToRedis() {
        noticeRepository.findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc().map { it.toRedisEntity() }.toList().also { topList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_NOTICE_RECENT,
                value = topList,
                typeReference = object : TypeReference<List<RedisNotice>>() {}
            )
        }
    }

    private suspend fun addBannerToRedis(redisEntity: RedisNotice) {
        redisRepository.addOrUpdateRBucket(
            bucketKey = CMS_NOTICE_BANNER,
            value = redisEntity,
            typeReference = object : TypeReference<RedisNotice>() {}
        )
    }

    private suspend fun deleteBannerFromRedis() {
        redisRepository.deleteRBucket(
            bucketKey = CMS_NOTICE_BANNER,
            typeReference = object : TypeReference<RedisNotice>() {}
        )
    }

    private suspend fun findAndDeleteAllBanner(account: Account) {
        noticeRepository.findAllByIsBannerIsTrueAndIsDraftIsFalseAndIsScheduleIsFalse().map { notice ->
            notice.isBanner = false
            notice.setUpdateInfo(account)
            return@map notice
        }.toList().also { noticeList ->
            noticeRepository.saveAll(noticeList).collect()
        }
    }

    suspend fun getNotices(searchParams: SearchParams, account: Account): Result<ListResponse<NoticeResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<NoticeResponse>> = async {
                noticeRepository.findAllByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                )
                    .map { it.toDraftResponse() }
                    .toList()
            }

            val notices: Deferred<List<NoticeResponse>> = async {
                noticeRepository.findAllByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            val top: Deferred<List<NoticeResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                noticeRepository.findAllByCriteria(criteria = criteria.withoutDraft(), pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(notices.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(notices.await().size.toLong())
            )
        }
    }

    suspend fun getNotice(id: String): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updateNotice(
        id: String,
        request: NoticeRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<NoticeDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && request.validateNotice() && fileRequest?.validate() == true
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

                noticeRepository.findById(id)?.let {
                    val isBannerChange: Boolean = it.isBanner != request.isBanner
                    val isChange: Boolean = it.isFixTop != request.isFixTop

                    if (isBannerChange) {
                        when (request.isBanner) {
                            true -> {
                                findAndDeleteAllBanner(account)
                                addBannerToRedis(it.toRedisEntity())
                            }

                            false -> {
                                deleteBannerFromRedis()
                            }
                        }
                    }
                    it.setUpdateInfo(request = request, account = account, fileRequest = fileRequest)
                    noticeRepository.save(it).toResponse().also {
                        if (isChange) {
                            applyToRedis()
                        }
                        applyTop5ToRedis()
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deleteNotice(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    noticeRepository.save(it).toResponse().also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                        applyTop5ToRedis()
                    }
                }
            }
        }
    }

    @Transactional
    suspend fun createNoticeBanner(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
            it.isBanner = true
            it.setUpdateInfo(account)
            findAndDeleteAllBanner(account)
            noticeRepository.save(it).toResponse().also { response ->
                addBannerToRedis(response.toRedisEntity())
            }
        }
    }

    @Transactional
    suspend fun deleteNoticeBanner(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        noticeRepository.findById(id)?.let {
            it.isBanner = false
            it.setUpdateInfo(account)
            noticeRepository.save(it).toResponse().also { deleteBannerFromRedis() }
        }
    }
}
