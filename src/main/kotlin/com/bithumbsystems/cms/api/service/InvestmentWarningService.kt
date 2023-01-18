package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_INVESTMENT_WARNING_FIX
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsInvestmentWarningRepository
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
class InvestmentWarningService(
    private val investmentWarningRepository: CmsInvestmentWarningRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository,
    private val awsProperties: AwsProperties
) : CmsBaseService() {

    /**
     * 투자유의지정 안내 생성
     * @param request 투자유의지정 안내 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    @Transactional
    suspend fun createInvestmentWarning(
        request: InvestmentWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<InvestmentWarningDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = investmentWarningRepository
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
                            ivKey = awsProperties.ivKey, fileRequest = fileRequest, account = account
                        )
                    }
                } ?: request.setCreateInfo(
                    password = awsProperties.kmsKey,
                    saltKey = awsProperties.saltKey,
                    ivKey = awsProperties.ivKey,
                    account = account
                )

                investmentWarningRepository.save(request.toEntity()).toResponse(awsProperties.kmsKey).also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    suspend fun getInvestmentWarnings(searchParams: SearchParams, account: Account): Result<ListResponse<InvestmentWarningResponse>?, ErrorData> =
        executeIn {
            coroutineScope {
                var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
                val defaultSort: Sort = buildSort()

                val drafts: Deferred<List<InvestmentWarningResponse>> = async {
                    investmentWarningRepository.findByCriteria(
                        criteria = QueryUtil.buildCriteriaForDraft(account.accountId),
                        pageable = Pageable.unpaged(),
                        sort = QueryUtil.buildSortForDraft()
                    )
                        .map { it.toDraftResponse() }
                        .toList()
                }

                val investmentWarnings: Deferred<List<InvestmentWarningResponse>> = async {
                    investmentWarningRepository.findByCriteria(
                        criteria = criteria.withoutDraft(),
                        pageable = Pageable.unpaged(),
                        sort = defaultSort
                    )
                        .map { it.toMaskingResponse(awsProperties.kmsKey) }
                        .toList()
                }

                val top: Deferred<List<InvestmentWarningResponse>> = async {
                    criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                    investmentWarningRepository.findByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                        .map { it.toMaskingResponse(awsProperties.kmsKey) }
                        .toList()
                }

                ListResponse(
                    contents = top.await().plus(drafts.await()).plus(investmentWarnings.await()),
                    totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(investmentWarnings.await().size.toLong())
                )
            }
        }

    suspend fun getInvestmentWarning(id: String): Result<InvestmentWarningDetailResponse?, ErrorData> = executeIn {
        investmentWarningRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    @Transactional
    suspend fun updateInvestmentWarning(
        id: String,
        request: InvestmentWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<InvestmentWarningDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = investmentWarningRepository
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

                investmentWarningRepository.findById(id)?.let {
                    val isChange: Boolean =
                        it.isFixTop != request.isFixTop || it.isDelete != request.isDelete || it.isShow != request.isShow ||
                            it.isSchedule != request.isSchedule
                    it.setUpdateInfo(
                        password = awsProperties.kmsKey,
                        saltKey = awsProperties.saltKey,
                        ivKey = awsProperties.ivKey,
                        request = request,
                        account = account,
                        fileRequest = fileRequest
                    )
                    investmentWarningRepository.save(it).toResponse(awsProperties.kmsKey).also {
                        if (isChange) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deleteInvestmentWarning(id: String, account: Account): Result<InvestmentWarningDetailResponse?, ErrorData> = executeIn {
        investmentWarningRepository.findById(id)?.let {
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
                    investmentWarningRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }

    private suspend fun applyToRedis() {
        investmentWarningRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_INVESTMENT_WARNING_FIX,
                value = totalList.first().id,
                typeReference = object : TypeReference<String>() {}
            )
        }
    }
}
