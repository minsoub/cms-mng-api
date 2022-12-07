package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCategoryRepository
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
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeCategoryService(
    private val noticeCategoryRepository: CmsNoticeCategoryRepository,
    private val noticeCustomRepository: CmsCustomRepository<CmsNoticeCategory>
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

        noticeCategoryRepository.save(entity).toResponse()
    }

    /**
     * 공지사항 카테고리 목록을 조회한다.
     * @param searchParams 검색 조건
     */
    suspend fun getCategories(searchParams: SearchParams): Result<PageResponse<NoticeCategoryResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null)
            val sort: Sort = searchParams.buildSort()
            val count: Deferred<Long> = async {
                noticeCustomRepository.countAllByCriteria(criteria)
            }
            val countPerPage: Int = searchParams.pageSize!!

            val categories: Deferred<List<NoticeCategoryResponse>> = async {
                noticeCustomRepository.findAllByCriteria(criteria, PageRequest.of(searchParams.page!!, countPerPage), sort)
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            PageResponse(
                contents = categories.await(),
                totalCounts = count.await(),
                currentPage = searchParams.page!!,
                pageSize = countPerPage
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
            category?.setUpdateInfo(request = request, account = account)

            category?.let {
                noticeCategoryRepository.save(category).toResponse()
            }
        }

    /**
     * 카테고리 삭제
     * @param id 삭제할 카테고리 아이디
     * @param account 요청자 계정 정보
     */
    @Transactional
    suspend fun deleteCategory(id: String, account: Account): Result<Unit?, ErrorData> = executeIn {
        val category: CmsNoticeCategory? = getCategory(id).component1()?.toEntity()

        if (category?.isDelete == true) Unit
        else {
            category?.isDelete = true
            category?.setUpdateInfo(account)

            category?.let {
                noticeCategoryRepository.save(category)
                toDeleteResponse()
            }
        }
    }
}
