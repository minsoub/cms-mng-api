package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.constants.PageConstants.FIX_MAX_SIZE
import com.bithumbsystems.cms.api.model.enums.RedisKeys
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.Logger
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.api.util.QueryUtil.buildSortForFix
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.entity.setUpdateInfo
import com.bithumbsystems.cms.persistence.mongo.entity.toRedisEntity
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeRepository
import com.bithumbsystems.cms.persistence.redis.entity.RedisNotice
import com.bithumbsystems.cms.persistence.redis.repository.RedisRepository
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
class NoticeService(
    private val noticeRepository: CmsNoticeRepository,
    private val noticeCustomRepository: CmsCustomRepository<CmsNotice>,
    private val fileService: FileService,
    private val redisRepository: RedisRepository
) {
    val logger by Logger()

    /**
     * 공지사항 생성
     * @param request 공지사항 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     * @param account 생성자 정보
     */
    @Transactional
    suspend fun createNotice(
        request: NoticeRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<NoticeDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            coroutineScope {
                fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

                request.createAccountId = account.accountId
                request.createAccountEmail = account.email

                val entity: CmsNotice = request.toEntity()

                noticeRepository.save(entity).toResponse().also {
                    if (it.isFixTop) {
                        redisRepository.addRListValue(
                            listKey = RedisKeys.CMS_NOTICE_FIX,
                            value = entity.toRedisEntity(),
                            clazz = RedisNotice::class.java
                        )
                        // todo fixed만 레디스로 등록
                    }
                }
            }
        }
    )

    suspend fun getNotices(searchParams: SearchParams): Result<PageResponse<NoticeResponse>?, ErrorData> = executeIn {
        coroutineScope {
            var criteria: Criteria = searchParams.buildCriteria(isFixTop = false, isDelete = false)
            val sort: Sort = searchParams.buildSort()
            val count: Deferred<Long> = async {
                noticeCustomRepository.countAllByCriteria(criteria)
            }
            val countPerPage: Int = searchParams.pageSize ?: 0

            val notices: Deferred<List<NoticeResponse>> = async {
                noticeCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = PageRequest.of(searchParams.page ?: 0, countPerPage),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            val top: Deferred<List<NoticeResponse>> = async {
                criteria = searchParams.buildCriteria(isFixTop = true, isDelete = false)
                noticeCustomRepository.findAllByCriteria(criteria = criteria, pageable = PageRequest.ofSize(FIX_MAX_SIZE), sort = buildSortForFix())
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            PageResponse(
                contents = top.await().plus(notices.await()),
                totalCounts = count.await(),
                currentPage = searchParams.page ?: 0,
                pageSize = countPerPage
            )
        }
    }

    suspend fun getNotice(id: String) = executeIn {
        noticeRepository.findById(id)?.toResponse()
    }

    @Transactional
    suspend fun updateNotice(
        id: String,
        request: NoticeRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<NoticeDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            getNotice(id).component1()?.toEntity()?.let {
                it.setUpdateInfo(request = request, account = account)
                noticeRepository.save(it).toResponse() // todo fixed redis
            }
        }
    )

    @Transactional
    suspend fun deleteNotice(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        getNotice(id).component1()?.toEntity()?.let {
            when (it.isDelete) {
                true -> it.toResponse()
                false -> {
                    it.isDelete = true
                    it.setUpdateInfo(account)
                    noticeRepository.save(it).toResponse() // todo fixed redis
                }
            }
        }
    }

    @Transactional
    suspend fun setNoticeBanner(id: String, account: Account): Result<NoticeDetailResponse?, ErrorData> = executeIn {
        getNotice(id).component1()?.toEntity()?.let {
            when (it.isBanner) {
                true -> it.toResponse()
                else -> {
                    it.isBanner = true
                    it.setUpdateInfo(account)
                    noticeRepository.save(it).toResponse() // todo 배너 redis
                }
            }
        }
    }
}
