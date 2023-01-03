package com.bithumbsystems.cms.api.service

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
import com.bithumbsystems.cms.persistence.mongo.repository.CmsInvestWarningRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisBoard
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
class InvestWarningService(
    private val investWarningRepository: CmsInvestWarningRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {

    /**
     * 투자유의지정 안내 생성
     * @param request 투자유의지정 안내 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    @Transactional
    suspend fun createInvestWarning(
        request: InvestWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<InvestWarningDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() == true
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

                investWarningRepository.save(request.toEntity()).toResponse().also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                }
            }
        }
    )

    private suspend fun applyToRedis() {
        investWarningRepository.getFixItems().map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_INVESTMENT_WARNING_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
    }

    suspend fun getInvestWarnings(searchParams: SearchParams, account: Account): Result<ListResponse<InvestWarningResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<InvestWarningResponse>> = async {
                investWarningRepository.findAllByCriteria(
                    criteria = QueryUtil.buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = QueryUtil.buildSortForDraft()
                )
                    .map { it.toDraftResponse() }
                    .toList()
            }

            val investWarnings: Deferred<List<InvestWarningResponse>> = async {
                investWarningRepository.findAllByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            val top: Deferred<List<InvestWarningResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                investWarningRepository.findAllByCriteria(criteria = criteria, pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(investWarnings.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(investWarnings.await().size.toLong())
            )
        }
    }

    suspend fun getInvestWarning(id: String): Result<InvestWarningDetailResponse?, ErrorData> = executeIn {
        investWarningRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updateInvestWarning(
        id: String,
        request: InvestWarningRequest,
        fileRequest: FileRequest?,
        account: Account
    ) = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() == true
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

                investWarningRepository.findById(id)?.let {
                    val isChange: Boolean = it.isFixTop != request.isFixTop
                    it.setUpdateInfo(request = request, account = account, fileRequest = fileRequest)
                    investWarningRepository.save(it).toResponse().also {
                        if (isChange) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deleteInvestWarning(id: String, account: Account): Result<InvestWarningDetailResponse?, ErrorData> = executeIn {
        investWarningRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    investWarningRepository.save(it).toResponse().also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                    }
                }
            }
        }
    }
}
