package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsInvestWarning
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsInvestWarningRepository
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
class InvestWarningService(
    private val investWarningRepository: CmsInvestWarningRepository,
    private val investWarningCustomRepository: CmsCustomRepository<CmsInvestWarning>,
    private val fileService: FileService
) {

    /**
     * 투자유의지정 안내 생성
     * @param request 투자유의지정 안내 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    suspend fun createInvestWarning(
        request: InvestWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<InvestWarningDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            investWarningRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun getInvestWarnings(searchParams: SearchParams): Result<PageResponse<InvestWarningResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
            val sort: Sort = searchParams.buildSort()
            val count: Deferred<Long> = async {
                investWarningCustomRepository.countAllByCriteria(criteria)
            }
            val countPerPage: Int = searchParams.pageSize!!

            val investWarnings: Deferred<List<InvestWarningResponse>> = async {
                investWarningCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = PageRequest.of(searchParams.page!!, countPerPage),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            // todo 로직들 반영

            PageResponse(
                contents = investWarnings.await(),
                totalCounts = count.await(),
                currentPage = searchParams.page!!,
                pageSize = countPerPage
            )
        }
    }

    suspend fun getInvestWarning(id: String): Result<InvestWarningDetailResponse?, ErrorData> = executeIn {
        investWarningRepository.findById(id)?.toResponse()
    }

    suspend fun updateInvestWarning(
        id: String,
        request: InvestWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ) = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            println("$id, $account")

            investWarningRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun deleteInvestWarning(id: String, account: Account): Result<InvestWarningDetailResponse?, ErrorData> = executeIn {
        val investWarning: CmsInvestWarning? = getInvestWarning(id).component1()?.toEntity()

        println(account)

        investWarning?.let {
            investWarningRepository.save(investWarning).toResponse() // todo
        }
    }
}
