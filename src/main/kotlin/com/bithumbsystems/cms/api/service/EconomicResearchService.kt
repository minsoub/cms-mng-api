package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
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
class EconomicResearchService(
    private val economicResearchRepository: CmsEconomicResearchRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository,
    private val awsProperties: AwsProperties
) : CmsBaseService() {

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
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = economicResearchRepository
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
                        request.thumbnailFileId = fileRequest.thumbnailFileKey
                    }
                } ?: request.setCreateInfo(
                    password = awsProperties.kmsKey,
                    saltKey = awsProperties.saltKey,
                    ivKey = awsProperties.ivKey,
                    account = account
                )

                economicResearchRepository.save(request.toEntity()).toResponse(awsProperties.kmsKey).also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    suspend fun getEconomicResearches(searchParams: SearchParams, account: Account): Result<ListResponse<EconomicResearchResponse>?, ErrorData> =
        executeIn {
            coroutineScope {
                var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
                val defaultSort: Sort = buildSort()

                val drafts: Deferred<List<EconomicResearchResponse>> = async {
                    economicResearchRepository.findByCriteria(
                        criteria = buildCriteriaForDraft(account.accountId),
                        pageable = Pageable.unpaged(),
                        sort = buildSortForDraft()
                    )
                        .map { it.toDraftResponse() }
                        .toList()
                }

                val economicResearches: Deferred<List<EconomicResearchResponse>> = async {
                    economicResearchRepository.findByCriteria(
                        criteria = criteria.withoutDraft(),
                        pageable = Pageable.unpaged(),
                        sort = defaultSort
                    )
                        .map { it.toMaskingResponse(awsProperties.kmsKey) }
                        .toList()
                }

                val top: Deferred<List<EconomicResearchResponse>> = async {
                    criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                    economicResearchRepository.findByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                        .map { it.toMaskingResponse(awsProperties.kmsKey) }
                        .toList()
                }

                ListResponse(
                    contents = top.await().plus(drafts.await()).plus(economicResearches.await()),
                    totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(economicResearches.await().size.toLong())
                )
            }
        }

    suspend fun getEconomicResearch(id: String): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        economicResearchRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    @Transactional
    suspend fun updateEconomicResearch(
        id: String,
        request: EconomicResearchRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = economicResearchRepository
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
                economicResearchRepository.findById(id)?.let {
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
                    economicResearchRepository.save(it).toResponse(awsProperties.kmsKey).also {
                        if (isChange) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deleteEconomicResearch(id: String, account: Account): Result<EconomicResearchDetailResponse?, ErrorData> = executeIn {
        economicResearchRepository.findById(id)?.let {
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
                    economicResearchRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }

    private suspend fun applyToRedis() {
        economicResearchRepository.getFixItems().map { item -> item.toRedisEntity() }.also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_ECONOMIC_RESEARCH_FIX,
                value = totalList.toList(),
                typeReference = object : TypeReference<List<RedisThumbnail>>() {}
            )
        }.collect()
    }
}
