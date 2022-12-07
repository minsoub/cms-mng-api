package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.service.NoticeCategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/notices")
class NoticeController(
    val noticeCategoryService: NoticeCategoryService
) {
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
        noticeCategoryService.createCategory(request, account)
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
            Parameter(description = "이동할 페이지", name = "page", `in` = ParameterIn.QUERY, schema = Schema(implementation = Int::class)),
            Parameter(description = "페이지 사이즈", name = "page_size", `in` = ParameterIn.QUERY, schema = Schema(implementation = Int::class)),
            Parameter(description = "검색어", name = "query", `in` = ParameterIn.QUERY, schema = Schema(implementation = String::class)),
            Parameter(description = "카테고리 아이디", name = "category_id", `in` = ParameterIn.QUERY, schema = Schema(implementation = String::class)),
            Parameter(description = "배너 여부", name = "is_banner", `in` = ParameterIn.QUERY, schema = Schema(implementation = Boolean::class)),
            Parameter(description = "사용 여부", name = "is_show", `in` = ParameterIn.QUERY, schema = Schema(implementation = Boolean::class)),
            Parameter(description = "상태(카테고리)", name = "is_use", `in` = ParameterIn.QUERY, schema = Schema(implementation = Boolean::class)),
            Parameter(description = "정렬 타겟", name = "sort_by", `in` = ParameterIn.QUERY, schema = Schema(implementation = SortBy::class)),
            Parameter(
                description = "정렬 방향",
                name = "sort_direction",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortDirection::class)
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
        noticeCategoryService.updateCategory(id, request, account)
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
                content = [Content(schema = Schema(implementation = Unit::class))]
            )
        ]
    )
    suspend fun deleteNoticeCategory(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        noticeCategoryService.deleteCategory(id, account)
    }
}
