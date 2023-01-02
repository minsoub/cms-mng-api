package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_PRESS_RELEASE_FIX
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_PRESS_RELEASE_RECENT
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteriaForDraft
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForDraft
import com.bithumbsystems.cms.api.util.withoutDraft
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsPressReleaseRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisBoard
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
class PressReleaseService(
    private val pressReleaseRepository: CmsPressReleaseRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {

    /**
     * 보도자료 생성
     * @param request 보도자료 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    @Transactional
    suspend fun createPressRelease(
        request: PressReleaseRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<PressReleaseDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)
            request.setCreateInfo(account)

            pressReleaseRepository.save(request.toEntity()).toResponse().also {
                if (it.isFixTop) {
                    applyToRedis()
                }
                applyTop5ToRedis()
            }
        }
    )

    private suspend fun applyToRedis() {
        pressReleaseRepository.getFixItems().map { it.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_PRESS_RELEASE_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
    }

    private suspend fun applyTop5ToRedis() {
        pressReleaseRepository.findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc().map { it.toRedisEntity() }.toList().also { topList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_PRESS_RELEASE_RECENT,
                value = topList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
    }

    suspend fun getPressReleases(searchParams: SearchParams, account: Account): Result<ListResponse<PressReleaseResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<PressReleaseResponse>> = async {
                pressReleaseRepository.findAllByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                ).map { it.toDraftResponse() }.toList()
            }

            val pressReleases: Deferred<List<PressReleaseResponse>> = async {
                pressReleaseRepository.findAllByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            val top: Deferred<List<PressReleaseResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                pressReleaseRepository.findAllByCriteria(criteria = criteria.withoutDraft(), pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(pressReleases.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(pressReleases.await().size.toLong())
            )
        }
    }

    suspend fun getPressRelease(id: String): Result<PressReleaseDetailResponse?, ErrorData> = executeIn {
        pressReleaseRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updatePressRelease(
        id: String,
        request: PressReleaseRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<PressReleaseDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            pressReleaseRepository.findById(id)?.let {
                val isChange: Boolean = it.isFixTop != request.isFixTop
                it.setUpdateInfo(request = request, account = account)
                pressReleaseRepository.save(it).toResponse().also {
                    if (isChange) {
                        applyToRedis()
                    }
                    applyTop5ToRedis()
                }
            }
        }
    )

    @Transactional
    suspend fun deletePressRelease(id: String, account: Account): Result<PressReleaseDetailResponse?, ErrorData> = executeIn {
        pressReleaseRepository.findById(id)?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    pressReleaseRepository.save(it).toResponse().also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                        applyTop5ToRedis()
                    }
                }
            }
        }
    }
}