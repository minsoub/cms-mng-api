package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsReviewReport
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsReviewReportRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class ReviewReportService(
    private val reviewReportRepository: CmsReviewReportRepository,
    private val reviewReportCustomRepository: CmsCustomRepository<CmsReviewReport>,
    private val fileService: FileService
) {

    /**
     * 검토보고서 생성
     * @param request 검토보고서 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    suspend fun createReviewReport(
        request: ReviewReportRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<ReviewReportDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
            uploadThumbnailFile(fileRequest, account, request)

            reviewReportRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun getReviewReports(searchParams: SearchParams) = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
            val sort: Sort = searchParams.buildSort()
            val count: Deferred<Long> = async {
                reviewReportCustomRepository.countAllByCriteria(criteria)
            }
            val countPerPage: Int = searchParams.pageSize!!

            val reviewReports: Deferred<List<ReviewReportResponse>> = async {
                reviewReportCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = PageRequest.of(searchParams.page!!, countPerPage),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            // todo 로직들 반영

            PageResponse(
                contents = reviewReports.await(),
                totalCounts = count.await(),
                currentPage = searchParams.page!!,
                pageSize = countPerPage
            )
        }
    }

    suspend fun getReviewReport(id: String) = executeIn {
        reviewReportRepository.findById(id)?.toResponse()
    }

    suspend fun updateReviewReport(
        id: String,
        request: ReviewReportRequest,
        fileRequest: FileRequest?,
        account: Account
    ) = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
            uploadThumbnailFile(fileRequest, account, request)

            println("$id, $account")

            reviewReportRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    private suspend fun uploadThumbnailFile(
        fileRequest: FileRequest?,
        account: Account,
        request: ReviewReportRequest
    ) {
        fileRequest?.thumbnailFile?.let { thumbnailFile ->
            fileService.addFileInfo(thumbnailFile, account).component1()?.let { fileResponse ->
                request.thumbnailFileId = fileResponse.id
            }
        }
    }

    suspend fun deleteReviewReport(id: String, account: Account) = executeIn {
        val reviewReport: CmsReviewReport? = getReviewReport(id).component1()?.toEntity()

        println(account)

        reviewReport?.let {
            reviewReportRepository.save(reviewReport).toResponse() // todo
        }
    }
}
