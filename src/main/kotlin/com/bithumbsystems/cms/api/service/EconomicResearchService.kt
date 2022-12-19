package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEconomicResearch
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsEconomicResearchRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class EconomicResearchService(
    private val economicResearchRepository: CmsEconomicResearchRepository,
    private val economicResearchCustomRepository: CmsCustomRepository<CmsEconomicResearch>,
    private val fileService: FileService
) {

    /**
     * 빗썸 경제연구소 생성
     * @param request 빗썸 경제연구소 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    suspend fun createEconomicResearch(
        request: EconomicResearchRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
            uploadThumbnailFile(fileRequest, account, request)

            economicResearchRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun getEconomicResearches(searchParams: SearchParams): Result<ListResponse<EconomicResearchResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
            val sort: Sort = searchParams.buildSort()

            val economicResearches: Deferred<List<EconomicResearchResponse>> = async {
                economicResearchCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = Pageable.unpaged(),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            // todo 로직들 반영

            ListResponse(
                contents = economicResearches.await(),
                totalCounts = economicResearches.await().size.toLong()
            )
        }
    }

    suspend fun getEconomicResearch(id: String): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        economicResearchRepository.findById(id)?.toResponse()
    }

    suspend fun updateEconomicResearch(
        id: String,
        request: EconomicResearchRequest,
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

            economicResearchRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    private suspend fun uploadThumbnailFile(
        fileRequest: FileRequest?,
        account: Account,
        request: EconomicResearchRequest
    ) {
        fileRequest?.thumbnailFile?.let { thumbnailFile ->
            fileService.addFileInfo(thumbnailFile, account).component1()?.let { fileResponse ->
                request.thumbnailFileId = fileResponse.id
            }
        }
    }

    suspend fun deleteEconomicResearch(id: String, account: Account): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        val economicResearch: CmsEconomicResearch? = getEconomicResearch(id).component1()?.toEntity()

        println(account)

        economicResearch?.let {
            economicResearchRepository.save(economicResearch).toResponse() // todo
        }
    }
}
