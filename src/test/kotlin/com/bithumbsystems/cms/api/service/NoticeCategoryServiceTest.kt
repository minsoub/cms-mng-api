package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.RedisKeys.CMS_NOTICE_CATEGORY
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.ListResponse
import com.bithumbsystems.cms.api.model.response.NoticeCategoryDetailResponse
import com.bithumbsystems.cms.api.model.response.NoticeCategoryResponse
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCategoryRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisNoticeCategory
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
import com.github.michaelbull.result.Result
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
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
    private lateinit var redisRepository: RedisRepository
    private lateinit var awsProperties: AwsProperties

    private val randomUUID = UUID.randomUUID().toString().replace("-", "")
    private lateinit var account: Account
    private lateinit var request: NoticeCategoryRequest
    private val ivKey = "6xCNtHMFNjLk1ldO"
    private val saltKey = "3vVDyR_6aCONXL65j5f0CA"
    private val kmsKey = "eee6a9bd-4abd-4803-90bd-d943a019"

    @BeforeAll
    fun beforeAll() {
        noticeCategoryRepository = mockk()
        // noticeCustomRepository = mockk()
        redisRepository = mockk()
        awsProperties = mockk()
        coEvery {
            awsProperties.ivKey
        } returns ivKey
        coEvery {
            awsProperties.saltKey
        } returns saltKey
        coEvery {
            awsProperties.kmsKey
        } returns kmsKey
        noticeCategoryService = spyk(
            objToCopy = NoticeCategoryService(noticeCategoryRepository, redisRepository, awsProperties),
            recordPrivateCalls = true
        )
        account = Account(accountId = "id", email = "user@example.com", userIp = "127.0.0.1", roles = setOf("admin"), mySiteId = "siteId")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 생성 테스트`() = runTest {
        request = NoticeCategoryRequest(name = randomUUID, isUse = true)
        request.createAccountId = account.accountId
        request.createAccountEmail = account.email
        val entity = request.toEntity()

        coEvery {
            noticeCategoryRepository.save(any())
        } returns entity

        coEvery {
            noticeCategoryRepository.findByCriteria(any(), any(), any())
        } returns flowOf(entity)

        coJustRun {
            redisRepository.addOrUpdateRBucket<RedisNoticeCategory>(
                bucketKey = CMS_NOTICE_CATEGORY,
                value = any(),
                typeReference = any()
            )
        }
        val result: Result<NoticeCategoryDetailResponse?, ErrorData> = noticeCategoryService.createCategory(request, account)

        result.component1()?.id `should be equal to` entity.id
        result.component1()?.name `should be equal to` randomUUID
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 목록 조회 테스트`(): Unit = runTest {
        val searchParams = SearchParams(page = 0, query = randomUUID, isUse = true, pageSize = 1)
        val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
        val sort: Sort = searchParams.buildSort()

        coEvery {
            searchParams.page?.let { PageRequest.of(it, 1) }?.let { noticeCategoryRepository.findByCriteria(criteria, it, sort) }
        } returns flowOf(NoticeCategoryRequest(name = randomUUID, isUse = false).toEntity())

        val result: Result<ListResponse<NoticeCategoryResponse>?, ErrorData> = noticeCategoryService.getCategories(searchParams)

        result.component1()?.contents!![0].name `should be equal to` randomUUID
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 조회 테스트`(): Unit = runTest {
        val entity: CmsNoticeCategory = NoticeCategoryRequest(name = randomUUID, isUse = true).toEntity()

        coEvery {
            noticeCategoryRepository.findById(entity.id)
        } returns entity

        val result: Result<NoticeCategoryDetailResponse?, ErrorData> = noticeCategoryService.getCategory(entity.id)

        result.component1()?.id `should be equal to` entity.id
        result.component1()?.name `should be equal to` randomUUID
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `1번 카테고리 수정 테스트`(): Unit = runTest {
        val newName: String = UUID.randomUUID().toString().replace("-", "")
        val entity: CmsNoticeCategory = NoticeCategoryRequest(name = randomUUID, isUse = true, isDelete = false).toEntity()

        coEvery {
            noticeCategoryRepository.findById(entity.id)
        } returns entity

        request = NoticeCategoryRequest(name = newName, isUse = false, isDelete = false)
        entity.setUpdateInfo(password = kmsKey, ivKey = ivKey, saltKey = saltKey, request = request, account = account)

        coEvery {
            noticeCategoryRepository.save(any())
        } returns entity

        coJustRun {
            redisRepository.addOrUpdateRBucket<RedisNoticeCategory>(
                bucketKey = CMS_NOTICE_CATEGORY,
                value = any(),
                typeReference = any()
            )
        }

        val result: Result<NoticeCategoryDetailResponse?, ErrorData> = noticeCategoryService.updateCategory(entity.id, request, account)

        result.component1()?.id `should be equal to` entity.id
        result.component1()?.name `should be equal to` newName
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `2번 카테고리 수정 테스트`(): Unit = runTest {
        val entity: CmsNoticeCategory = NoticeCategoryRequest(name = randomUUID, isUse = true, isDelete = false).toEntity()

        coEvery {
            noticeCategoryRepository.findById(entity.id)
        } returns entity

        request = NoticeCategoryRequest(name = randomUUID, isUse = false, isDelete = true)
        entity.setUpdateInfo(password = kmsKey, ivKey = ivKey, saltKey = saltKey, request = request, account = account)

        coEvery {
            noticeCategoryRepository.save(any())
        } returns entity

        coEvery {
            noticeCategoryRepository.findByCriteria(any(), any(), any())
        } returns flowOf(entity)

        coJustRun {
            redisRepository.addOrUpdateRBucket<RedisNoticeCategory>(
                bucketKey = CMS_NOTICE_CATEGORY,
                value = any(),
                typeReference = any()
            )
        }

        val result: Result<NoticeCategoryDetailResponse?, ErrorData> = noticeCategoryService.updateCategory(entity.id, request, account)

        result.component1()?.id `should be equal to` entity.id
        result.component1()?.isUse `should be equal to` false
        result.component1()?.isDelete `should be equal to` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `카테고리 삭제 테스트`(): Unit = runTest {
        request = NoticeCategoryRequest(name = randomUUID, isUse = false)
        request.createAccountId = account.accountId
        request.createAccountEmail = account.email
        val entity = request.toEntity()

        coEvery {
            noticeCategoryRepository.findById(entity.id)
        } returns entity

        entity.isDelete = true
        entity.setUpdateInfo(password = kmsKey, ivKey = ivKey, saltKey = saltKey, account = account)

        coEvery {
            noticeCategoryRepository.save(any())
        } returns entity

        coJustRun {
            redisRepository.addOrUpdateRBucket<RedisNoticeCategory>(
                bucketKey = CMS_NOTICE_CATEGORY,
                value = any(),
                typeReference = any()
            )
        }

        val result: Result<NoticeCategoryDetailResponse?, ErrorData> = noticeCategoryService.deleteCategory(entity.id, account)

        result.component1()?.isDelete `should be` true
    }
}
