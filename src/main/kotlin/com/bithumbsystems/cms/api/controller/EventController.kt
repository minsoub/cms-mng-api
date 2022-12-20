package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.enums.EventType
import com.bithumbsystems.cms.api.model.request.EventDownloadRequest
import com.bithumbsystems.cms.api.model.request.EventRequest
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.response.EventDetailResponse
import com.bithumbsystems.cms.api.model.response.EventResponse
import com.bithumbsystems.cms.api.service.EventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import java.nio.ByteBuffer
import java.time.LocalDate

@RestController
@RequestMapping("/events")
class EventController(
    private val eventService: EventService
) {
    /**
     * 이벤트 생성
     * @param request 생성할 이벤트 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "이벤트 생성",
        description = "이벤트를 생성합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = EventDetailResponse::class))]
            )
        ]
    )
    suspend fun createEvent(
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: EventRequest,
        @RequestPart(value = "file", required = false) @Parameter(
            description = "첨부 파일",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        filePart: FilePart?,
        @RequestPart(value = "share_file", required = false) @Parameter(
            description = "공유 태그 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        shareFilePart: FilePart?,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        eventService.createEvent(
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @GetMapping
    @Operation(
        summary = "이벤트 목록 조회",
        description = "이벤트 목록을 조회합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = EventResponse::class))]
            )
        ],
        parameters = [
            Parameter(
                description = "제목/내용",
                name = "query",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "검색어"
            ),
            Parameter(
                description = "배너 여부",
                name = "is_banner",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "false"
            ),
            Parameter(
                description = "유형",
                name = "event_type",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = EventType::class),
                example = "DEFAULT"
            ),
            Parameter(
                description = "등록기간(시작일)",
                name = "start_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            ),
            Parameter(
                description = "등록기간(종료일)",
                name = "end_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            )
        ]
    )
    suspend fun getEvents(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams
    ) = execute {
        eventService.getEvents(searchParams)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "이벤트 조회",
        description = "이벤트 조회합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = EventDetailResponse::class))]
            )
        ]
    )
    suspend fun getEvent(@PathVariable id: String) = execute {
        eventService.getEvent(id)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "이벤트 수정",
        description = "이벤트를 수정합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = EventDetailResponse::class))]
            )
        ]
    )
    suspend fun updateEvent(
        @PathVariable id: String,
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: EventRequest,
        @RequestPart(value = "file", required = false) @Parameter(
            description = "첨부 파일",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        filePart: FilePart?,
        @RequestPart(value = "share_file", required = false) @Parameter(
            description = "공유 태그 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        shareFilePart: FilePart?,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        eventService.updateEvent(
            id = id,
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "이벤트 삭제",
        description = "이벤트를 삭제합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = EventDetailResponse::class))]
            )
        ]
    )
    suspend fun deleteEvent(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        eventService.deleteEvent(id = id, account = account)
    }

    @GetMapping("/{id}/excel")
    @Operation(
        summary = "이벤트 참여자 정보 다운로드",
        description = "이벤트 참여자 정보를 다운로드합니다.",
        tags = ["이벤트 > 이벤트 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "다운로드 성공",
                content = [Content(schema = Schema(implementation = ByteBuffer::class))]
            )
        ]
    )
    suspend fun downloadEventExcel(
        @PathVariable id: String,
        @RequestBody request: EventDownloadRequest,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        eventService.downloadEventExcel(id = id, request = request, account = account)
    }
}
