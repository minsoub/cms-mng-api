package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.NoticeCategoryResponse
import com.bithumbsystems.cms.api.model.response.PageResponse
import com.bithumbsystems.cms.api.model.response.toMaskingResponse
import com.bithumbsystems.cms.api.model.response.toResponse
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCategoryRepository
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
    private val noticeCustomRepository: CmsCustomRepository<CmsNoticeCategory>,
) {

    /**
     * 공지사항 카테고리를 생성한다.
     * @param request 공지사항 카테고리 등록 데이터
     * @param account 요청자 계정 정보
     */
    @Transactional
    suspend fun createCategory(request: NoticeCategoryRequest, account: Account) = executeIn {
        request.createAccountId = account.accountId
        request.createAccountEmail = account.email

        val entity: CmsNoticeCategory = request.toEntity()

        noticeCategoryRepository.save(entity).toResponse()
    }

    /**
     * 공지사항 카테고리 목록을 조회한다.
     * @param searchParams 검색 조건
     */
    suspend fun getCategories(searchParams: SearchParams) = executeIn {
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
}
