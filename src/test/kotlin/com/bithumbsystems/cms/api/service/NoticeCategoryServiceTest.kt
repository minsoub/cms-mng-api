package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.NoticeCategoryResponse
import com.bithumbsystems.cms.api.model.response.PageResponse
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCategoryRepository
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.github.michaelbull.result.Result
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
class NoticeCategoryServiceTest {

    private lateinit var noticeCategoryService: NoticeCategoryService
    private lateinit var noticeCategoryRepository: CmsNoticeCategoryRepository
    private lateinit var noticeCustomRepository: CmsCustomRepository<CmsNoticeCategory>
    private lateinit var redisRepository: RedisRepository

    private val randomUUID = UUID.randomUUID().toString().replace("-", "")

    @BeforeAll
    fun beforeAll() {
        noticeCategoryRepository = mockk()
        noticeCustomRepository = mockk()
        redisRepository = mockk()
        noticeCategoryService = spyk(
            objToCopy = NoticeCategoryService(noticeCategoryRepository, noticeCustomRepository),
            recordPrivateCalls = true
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 생성 테스트`() = runTest {
        val account =
            Account(accountId = "id", email = "user@example.com", userIp = "127.0.0.1", roles = setOf("admin"), mySiteId = "siteId")
        val request = NoticeCategoryRequest(name = randomUUID, isUse = false)
        request.createAccountId = account.accountId
        request.createAccountEmail = account.email
        val entity = request.toEntity()

        coEvery {
            noticeCategoryRepository.save(any())
        } returns entity

        val result: Result<NoticeCategoryResponse?, ErrorData> = noticeCategoryService.createCategory(request, account)

        result.component1()?.name `should be equal to` randomUUID
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 목록 조회 테스트`(): Unit = runTest {
        val searchParams = SearchParams(page = 0, query = randomUUID, isUse = false, pageSize = 1)
        val criteria: Criteria = searchParams.buildCriteria(isFixTop = null)
        val sort: Sort = searchParams.buildSort()

        coEvery {
            noticeCustomRepository.countAllByCriteria(criteria)
        } returns 1

        coEvery {
            searchParams.page?.let { PageRequest.of(it, 1) }?.let { noticeCustomRepository.findAllByCriteria(criteria, it, sort) }
        } returns flowOf(NoticeCategoryRequest(name = randomUUID, isUse = false).toEntity())

        val result: Result<PageResponse<NoticeCategoryResponse>?, ErrorData> = noticeCategoryService.getCategories(searchParams)

        result.component1()?.contents!![0].name `should be equal to` randomUUID
    }
}
