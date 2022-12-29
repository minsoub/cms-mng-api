package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_ECONOMIC_RESEARCH_FIX
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsEconomicResearchRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisThumbnail
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.fasterxml.jackson.core.type.TypeReference
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
import org.springframework.transaction.annotation.Transactional

@Service
class EconomicResearchService(
    private val economicResearchRepository: CmsEconomicResearchRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {

    /**
     * 빗썸 경제연구소 생성
     * @param request 빗썸 경제연구소 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    @Transactional
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
            uploadThumbnailFile(fileRequest, account, request) // todo

            request.setCreateInfo(account)

            economicResearchRepository.save(request.toEntity()).toResponse().also {
                if (it.isFixTop) {
                    applyToRedis()
                }
            }
        }
    )

    private suspend fun applyToRedis() {
        economicResearchRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_ECONOMIC_RESEARCH_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisThumbnail>>() {}
            )
        }
    }

    suspend fun getEconomicResearches(searchParams: SearchParams, account: Account): Result<ListResponse<EconomicResearchResponse>?, ErrorData> =
        executeIn {
            coroutineScope {
                var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
                val defaultSort: Sort = buildSort()

                val drafts: Deferred<List<EconomicResearchResponse>> = async {
                    economicResearchRepository.findAllByCriteria(
                        criteria = buildCriteriaForDraft(account.accountId),
                        pageable = Pageable.unpaged(),
                        sort = buildSortForDraft()
                    )
                        .map { it.toDraftResponse() }
                        .toList()
                }

                val economicResearches: Deferred<List<EconomicResearchResponse>> = async {
                    economicResearchRepository.findAllByCriteria(
                        criteria = criteria.withoutDraft(),
                        pageable = Pageable.unpaged(),
                        sort = defaultSort
                    )
                        .map { it.toMaskingResponse() }
                        .toList()
                }

                val top: Deferred<List<EconomicResearchResponse>> = async {
                    criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                    economicResearchRepository.findAllByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                        .map { it.toMaskingResponse() }
                        .toList()
                }

                ListResponse(
                    contents = top.await().plus(drafts.await()).plus(economicResearches.await()),
                    totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(economicResearches.await().size.toLong())
                )
            }
        }

    suspend fun getEconomicResearch(id: String): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        economicResearchRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updateEconomicResearch(
        id: String,
        request: EconomicResearchRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
            uploadThumbnailFile(fileRequest, account, request) // todo

            economicResearchRepository.findById(id)?.let {
                val isChange: Boolean = it.isFixTop != request.isFixTop
                it.setUpdateInfo(request = request, account = account)
                economicResearchRepository.save(it).toResponse().also {
                    if (isChange) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    private suspend fun uploadThumbnailFile(
        fileRequest: FileRequest?,
        account: Account,
        request: EconomicResearchRequest
    ) {
        fileRequest?.thumbnailFile?.let { thumbnailFile ->
            fileService.addFileInfo(file = thumbnailFile, account = account, fileSize = null).component1()?.let { fileResponse ->
                request.thumbnailFileId = fileResponse.id
            }
        }
    }

    @Transactional
    suspend fun deleteEconomicResearch(id: String, account: Account): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        economicResearchRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    economicResearchRepository.save(it).toResponse().also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }
}
