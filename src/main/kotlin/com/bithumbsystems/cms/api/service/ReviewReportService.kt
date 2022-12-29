package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_REVIEW_REPORT_FIX
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsReviewReportRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisThumbnail
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewReportService(
    private val reviewReportRepository: CmsReviewReportRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {

    /**
     * 검토보고서 생성
     * @param request 검토보고서 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    @Transactional
    suspend fun createReviewReport(
        request: ReviewReportRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<ReviewReportDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            coroutineScope {
                fileRequest?.let {
                    it.setKeys().also {
                        launch {
                            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
                        }
                        launch {
                            uploadThumbnailFile(fileRequest = fileRequest, account = account)
                        }
                        request.setCreateInfo(fileRequest = fileRequest, account = account)
                        request.thumbnailFileId = fileRequest.thumbnailFileKey
                    }
                } ?: request.setCreateInfo(account)

                reviewReportRepository.save(request.toEntity()).toResponse().also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    private suspend fun applyToRedis() {
        reviewReportRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_REVIEW_REPORT_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisThumbnail>>() {}
            )
        }
    }

    suspend fun getReviewReports(searchParams: SearchParams, account: Account): Result<ListResponse<ReviewReportResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<ReviewReportResponse>> = async {
                reviewReportRepository.findAllByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                )
                    .map { it.toDraftResponse() }
                    .toList()
            }

            val reviewReports: Deferred<List<ReviewReportResponse>> = async {
                reviewReportRepository.findAllByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            val top: Deferred<List<ReviewReportResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                reviewReportRepository.findAllByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(reviewReports.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(reviewReports.await().size.toLong())
            )
        }
    }

    suspend fun getReviewReport(id: String) = executeIn {
        reviewReportRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updateReviewReport(
        id: String,
        request: ReviewReportRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<ReviewReportDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            coroutineScope {
                fileRequest?.let {
                    it.setKeys().also {
                        launch {
                            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
                        }
                        launch {
                            uploadThumbnailFile(fileRequest = fileRequest, account = account)
                        }
                    }
                }

                reviewReportRepository.findById(id)?.let {
                    val isChange: Boolean = it.isFixTop != request.isFixTop
                    it.setUpdateInfo(request = request, account = account, fileRequest = fileRequest)
                    reviewReportRepository.save(it).toResponse().also {
                        if (isChange) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    )

    private suspend fun uploadThumbnailFile(
        fileRequest: FileRequest?,
        account: Account
    ) {
        fileRequest?.let {
            it.fileKey?.let { fileKey ->
                it.thumbnailFile?.let { thumbnailFile ->
                    fileService.addFileInfo(fileKey = fileKey, file = thumbnailFile, account = account, fileSize = null)
                }
            }
        }
    }

    @Transactional
    suspend fun deleteReviewReport(id: String, account: Account): Result<ReviewReportDetailResponse?, ErrorData> = executeIn {
        reviewReportRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    reviewReportRepository.save(it).toResponse().also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }
}
