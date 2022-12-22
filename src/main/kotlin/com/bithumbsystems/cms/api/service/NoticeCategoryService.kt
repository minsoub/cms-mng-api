package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_NOTICE_CATEGORY
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCategoryRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisNoticeCategory
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeCategoryService(
    private val noticeCategoryRepository: CmsNoticeCategoryRepository,
    private val redisRepository: RedisRepository
) {

    /**
     * 공지사항 카테고리를 생성한다.
     * @param request 공지사항 카테고리 등록 데이터
     * @param account 요청자 계정 정보
     */
    @Transactional
    suspend fun createCategory(request: NoticeCategoryRequest, account: Account): Result<NoticeCategoryDetailResponse?, ErrorData> = executeIn {
        request.createAccountId = account.accountId
        request.createAccountEmail = account.email

        val entity: CmsNoticeCategory = request.toEntity()

        noticeCategoryRepository.save(entity).toResponse().also {
            if (it.isUse) {
                applyToRedis()
            }
        }
    }

    private suspend fun applyToRedis() {
        noticeCategoryRepository.findAllByCriteria(
            criteria = Criteria.where("is_delete").`is`(false),
            pageable = Pageable.unpaged(),
            sort = buildSort()
        )
            .map { response -> response.toRedisEntity() }
            .toList().also { totalList ->
                redisRepository.addOrUpdateRBucket(
                    bucketKey = CMS_NOTICE_CATEGORY,
                    value = totalList,
                    typeReference = object : TypeReference<List<RedisNoticeCategory>>() {}
                )
            }
    }

    /**
     * 공지사항 카테고리 목록을 조회한다.
     * @param searchParams 검색 조건
     */
    suspend fun getCategories(searchParams: SearchParams): Result<ListResponse<NoticeCategoryResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)

            val categories: Deferred<List<NoticeCategoryResponse>> = async {
                noticeCategoryRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = Pageable.unpaged(),
                    sort = buildSort()
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = categories.await(),
                totalCounts = categories.await().size.toLong()
            )
        }
    }

    suspend fun getCategories(): Result<ListResponse<CategoryResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val categories: Deferred<List<CategoryResponse>> = async {
                noticeCategoryRepository.findAllByCriteria(
                    criteria = SearchParams(isUse = true).buildCriteria(isFixTop = null, isDelete = false),
                    pageable = Pageable.unpaged(),
                    sort = buildSort()
                )
                    .map { it.toCategoryMaskingResponse() }
                    .toList()
            }

            ListResponse(
                contents = categories.await(),
                totalCounts = categories.await().size.toLong()
            )
        }
    }

    /**
     * 카테고리 단일 조회
     * @param id 조회할 카테고리 아이디
     */
    suspend fun getCategory(id: String): Result<NoticeCategoryDetailResponse?, ErrorData> = executeIn {
        noticeCategoryRepository.findById(id)?.toResponse()
    }

    /**
     * 카테고리 수정
     * @param id 수정할 카테고리 아이디
     * @param request 수정 요청
     * @param account 수정 요청자 계정 정보
     */
    @Transactional
    suspend fun updateCategory(id: String, request: NoticeCategoryRequest, account: Account): Result<NoticeCategoryDetailResponse?, ErrorData> =
        executeIn {
            val category: CmsNoticeCategory? = getCategory(id).component1()?.toEntity()
            category?.let {
                category.setUpdateInfo(request = request, account = account)
                noticeCategoryRepository.save(category).toResponse().also {
                    applyToRedis()
                }
            }
        }

    /**
     * 카테고리 삭제
     * @param id 삭제할 카테고리 아이디
     * @param account 요청자 계정 정보
     */
    @Transactional
    suspend fun deleteCategory(id: String, account: Account): Result<NoticeCategoryDetailResponse?, ErrorData> = executeIn {
        getCategory(id).component1()?.toEntity()?.let {
            if (it.isDelete) it.toResponse()
            else {
                it.isDelete = true
                it.setUpdateInfo(account)

                noticeCategoryRepository.save(it).also {
                    applyToRedis()
                }.toResponse()
            }
        }
    }
}
