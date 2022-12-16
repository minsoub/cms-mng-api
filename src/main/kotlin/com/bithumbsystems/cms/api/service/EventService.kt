package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEvent
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsEventRepository
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

@Service
class EventService(
    private val eventRepository: CmsEventRepository,
    private val eventCustomRepository: CmsCustomRepository<CmsEvent>,
    private val fileService: FileService
) {
    /**
     * 이벤트 생성
     * @param request 이벤트 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     * @param account 생성자 정보
     */
    suspend fun createEvent(
        request: EventRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EventDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            eventRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun getEvents(searchParams: SearchParams) = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
            val sort: Sort = searchParams.buildSort()
            val count: Deferred<Long> = async {
                eventCustomRepository.countAllByCriteria(criteria)
            }
            val countPerPage: Int = searchParams.pageSize!!

            val notices: Deferred<List<EventResponse>> = async {
                eventCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = PageRequest.of(searchParams.page!!, countPerPage),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            // todo 로직들 반영

            PageResponse(
                contents = notices.await(),
                totalCounts = count.await(),
                currentPage = searchParams.page!!,
                pageSize = countPerPage
            )
        }
    }

    suspend fun getEvent(id: String) = executeIn {
        eventRepository.findById(id)?.toResponse()
    }

    suspend fun updateEvent(
        id: String,
        request: EventRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<EventDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            println(id)

            eventRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun deleteEvent(id: String, account: Account) = executeIn {
        val event: CmsEvent? = getEvent(id).component1()?.toEntity()

        println(account)

        event?.let {
            eventRepository.save(event).toResponse() // todo
        }
    }

    suspend fun downloadEventExcel(id: String, request: EventDownloadRequest, account: Account) = executeIn { println("$id, $request, $account") }
}
