package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
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
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisRecentEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsPressReleaseRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisBanner
import com.bithumbsystems.cms.persistence.redis.entity.RedisBoard
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PressReleaseService(
    private val pressReleaseRepository: CmsPressReleaseRepository,
    private val fileService: FileService,
    private val redisRepository: RedisRepository,
    private val awsProperties: AwsProperties
) : CmsBaseService() {

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
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = pressReleaseRepository
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
                    }
                } ?: request.setCreateInfo(
                    password = awsProperties.kmsKey,
                    saltKey = awsProperties.saltKey,
                    ivKey = awsProperties.ivKey,
                    account = account
                )

                pressReleaseRepository.save(request.toEntity()).toResponse(awsProperties.kmsKey).also {
                    if (it.isFixTop) {
                        applyToRedis()
                    }
                    applyTopToRedis(5)
                }
            }
        }
    )

    suspend fun getPressReleases(searchParams: SearchParams, account: Account): Result<ListResponse<PressReleaseResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val defaultSort: Sort = buildSort()

            val drafts: Deferred<List<PressReleaseResponse>> = async {
                pressReleaseRepository.findByCriteria(
                    criteria = buildCriteriaForDraft(account.accountId),
                    pageable = Pageable.unpaged(),
                    sort = buildSortForDraft()
                ).map { it.toDraftResponse() }.toList()
            }

            val pressReleases: Deferred<List<PressReleaseResponse>> = async {
                pressReleaseRepository.findByCriteria(
                    criteria = criteria.withoutDraft(),
                    pageable = Pageable.unpaged(),
                    sort = defaultSort
                )
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            val top: Deferred<List<PressReleaseResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                pressReleaseRepository.findByCriteria(criteria = criteria.withoutDraft(), pageable = Pageable.unpaged(), sort = defaultSort)
                    .map { it.toMaskingResponse(awsProperties.kmsKey) }
                    .toList()
            }

            ListResponse(
                contents = top.await().plus(drafts.await()).plus(pressReleases.await()),
                totalCounts = top.await().size.toLong().plus(drafts.await().size.toLong()).plus(pressReleases.await().size.toLong())
            )
        }
    }

    suspend fun getPressRelease(id: String): Result<PressReleaseDetailResponse?, ErrorData> = executeIn {
        pressReleaseRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    @Transactional
    suspend fun updatePressRelease(
        id: String,
        request: PressReleaseRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<PressReleaseDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate() && fileRequest?.validate() ?: true && validateScheduleDate(
                request = request,
                repository = pressReleaseRepository
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

                pressReleaseRepository.findById(id)?.let {
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
                    pressReleaseRepository.save(it).toResponse(awsProperties.kmsKey).also {
                        if (isChange) {
                            applyToRedis()
                        }
                        applyTopToRedis(5)
                    }
                }
            }
        }
    )

    @Transactional
    suspend fun deletePressRelease(id: String, account: Account): Result<PressReleaseDetailResponse?, ErrorData> = executeIn {
        pressReleaseRepository.findById(id)?.let {
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
                    pressReleaseRepository.save(it).toResponse(awsProperties.kmsKey).also { response ->
                        if (response.isFixTop) {
                            applyToRedis()
                        }
                        applyTopToRedis(5)
                    }
                }
            }
        }
    }

    private suspend fun applyToRedis() {
        pressReleaseRepository.getFixItems().map { it.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_PRESS_RELEASE_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
    }

    private suspend fun applyTopToRedis(limit: Int) {
        pressReleaseRepository.findByIsShowIsTrueAndIsDeleteIsFalseAndIsDraftIsFalseAndIsScheduleIsFalseOrderByScreenDateDesc(
            PageRequest.of(
                0,
                limit
            )
        ).map { it.toRedisRecentEntity() }.also { topList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = CMS_PRESS_RELEASE_RECENT,
                value = topList.toList(),
                typeReference = object : TypeReference<List<RedisBanner>>() {}
            )
        }.collect()
    }
}
