package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.NoticeRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.service.NoticeCategoryService
import com.bithumbsystems.cms.api.service.NoticeService
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
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/notices")
class NoticeController(
    private val noticeService: NoticeService,
    private val noticeCategoryService: NoticeCategoryService
) {
    /**
     * 공지사항 생성
     * @param request 생성할 공지사항 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "공지사항 생성",
        description = "공지사항을 생성합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = NoticeDetailResponse::class))]
            )
        ]
    )
    suspend fun createNotice(
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: NoticeRequest,
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
        noticeService.createNotice(
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @GetMapping("")
    @Operation(
        summary = "공지사항 목록 조회",
        description = "공지사항 목록을 조회합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = NoticeResponse::class))]
            )
        ],
        parameters = [
            Parameter(
                description = "이동할 페이지",
                name = "page",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "0"
            ),
            Parameter(
                description = "페이지 사이즈",
                name = "page_size",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "15"
            ),
            Parameter(
                description = "검색어",
                name = "query",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "검색어"
            ),
            Parameter(
                description = "카테고리 아이디",
                name = "category_id",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "5315d045f031424a8ca53128f344ac04"
            ),
            Parameter(
                description = "배너 여부",
                name = "is_banner",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "false"
            ),
            Parameter(
                description = "사용 여부",
                name = "is_show",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "상태(카테고리)",
                name = "is_use",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "정렬 타겟",
                name = "sort_by",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortBy::class),
                example = "SCREEN_DATE"
            ),
            Parameter(
                description = "정렬 방향",
                name = "sort_direction",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortDirection::class),
                example = "ASC"
            ),
            Parameter(
                description = "검색 시작일",
                name = "start_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            ),
            Parameter(
                description = "검색 종료일",
                name = "end_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            )
        ]
    )
    suspend fun getNotices(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams
    ) = execute {
        noticeService.getNotices(searchParams)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "공지사항 조회",
        description = "공지사항 조회합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = NoticeDetailResponse::class))]
            )
        ]
    )
    suspend fun getNotice(@PathVariable id: String) = execute {
        noticeService.getNotice(id)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "공지사항 수정",
        description = "공지사항을 수정합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = NoticeDetailResponse::class))]
            )
        ]
    )
    suspend fun updateNotice(
        @PathVariable id: String,
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: NoticeRequest,
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
        noticeService.updateNotice(
            id = id,
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "공지사항 삭제",
        description = "공지사항을 삭제합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = NoticeDetailResponse::class))]
            )
        ]
    )
    suspend fun deleteNotice(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeService.deleteNotice(id = id, account = account)
    }

    @PatchMapping("/{id}/banners")
    @Operation(
        summary = "공지사항 배너 등록",
        description = "공지사항 배너를 등록합니다.",
        tags = ["공지사항 > 공지사항 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = NoticeDetailResponse::class))]
            )
        ]
    )
    suspend fun setNoticeBanner(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeService.setNoticeBanner(id = id, account = account)
    }

    /**
     * 공지사항 카테고리 생성
     * @param request 생성할 공지사항 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping("/categories")
    @Operation(
        summary = "공지사항 카테고리 생성",
        description = "공지사항 카테고리를 생성합니다.",
        tags = ["공지사항 > 카테고리 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = NoticeCategoryDetailResponse::class))]
            )
        ]
    )
    suspend fun createNoticeCategory(
        @RequestBody request: NoticeCategoryRequest,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeCategoryService.createCategory(request = request, account = account)
    }

    /**
     * 공지사항 카테고리 목록 조회
     * @param searchParams 조회 조건
     */
    @GetMapping("/categories")
    @Operation(
        summary = "공지사항 카테고리 목록 조회",
        description = "공지사항 카테고리 목록을 조회합니다.",
        tags = ["공지사항 > 카테고리 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = NoticeCategoryResponse::class))]
            )
        ],
        parameters = [
            Parameter(
                description = "이동할 페이지",
                name = "page",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "0"
            ),
            Parameter(
                description = "페이지 사이즈",
                name = "page_size",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "15"
            ),
            Parameter(
                description = "검색어",
                name = "query",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "검색어"
            ),
            Parameter(
                description = "카테고리 아이디",
                name = "category_id",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "5315d045f031424a8ca53128f344ac04"
            ),
            Parameter(
                description = "배너 여부",
                name = "is_banner",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "false"
            ),
            Parameter(
                description = "사용 여부",
                name = "is_show",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "상태(카테고리)",
                name = "is_use",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "정렬 타겟",
                name = "sort_by",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortBy::class),
                example = "SCREEN_DATE"
            ),
            Parameter(
                description = "정렬 방향",
                name = "sort_direction",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortDirection::class),
                example = "ASC"
            ),
            Parameter(
                description = "검색 시작일",
                name = "start_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            ),
            Parameter(
                description = "검색 종료일",
                name = "end_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            )
        ]
    )
    suspend fun getNoticeCategories(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams
    ) = execute {
        noticeCategoryService.getCategories(searchParams)
    }

    @GetMapping("/categories/{id}")
    @Operation(
        summary = "공지사항 카테고리 조회",
        description = "공지사항 카테고리를 조회합니다.",
        tags = ["공지사항 > 카테고리 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = NoticeCategoryDetailResponse::class))]
            )
        ]
    )
    suspend fun getNoticeCategory(@PathVariable id: String) = execute {
        noticeCategoryService.getCategory(id)
    }

    @PutMapping("/categories/{id}")
    @Operation(
        summary = "공지사항 카테고리 수정",
        description = "공지사항 카테고리를 수정합니다.",
        tags = ["공지사항 > 카테고리 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = NoticeCategoryDetailResponse::class))]
            )
        ]
    )
    suspend fun updateNoticeCategory(
        @PathVariable id: String,
        @RequestBody request: NoticeCategoryRequest,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeCategoryService.updateCategory(id = id, request = request, account = account)
    }

    @DeleteMapping("/categories/{id}")
    @Operation(
        summary = "공지사항 카테고리 삭제",
        description = "공지사항 카테고리를 삭제합니다.",
        tags = ["공지사항 > 카테고리 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = NoticeCategoryDetailResponse::class))]
            )
        ]
    )
    suspend fun deleteNoticeCategory(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeCategoryService.deleteCategory(id = id, account = account)
    }
}
