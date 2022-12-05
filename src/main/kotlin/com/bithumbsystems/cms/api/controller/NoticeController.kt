package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.service.NoticeCategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/notices")
class NoticeController(
    val noticeCategoryService: NoticeCategoryService,
) {
    /**
     * 공지사항 카테고리 생성
     * @param request 생성할 공지사항 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping("/categories")
    @Operation(summary = "공지사항 카테고리 생성", description = "공지사항 카테고리를 생성합니다.", tags = ["공지사항 > 카테고리 관리 > 신규"])
    suspend fun createNoticeCategory(
        @RequestBody request: NoticeCategoryRequest,
        @Parameter(hidden = true) @CurrentUser
        account: Account,
    ) = execute {
        noticeCategoryService.createCategory(request, account)
    }

    /**
     * 공지사항 카테고리 목록 조회
     * @param searchParams 조회 조건
     */
    @GetMapping("/categories")
    @Operation(summary = "공지사항 카테고리 목록 조회", description = "공지사항 카테고리 목록을 조회합니다.", tags = ["공지사항 > 카테고리 관리"])
    suspend fun getNoticeCategories(@QueryParam searchParams: SearchParams) = execute {
        noticeCategoryService.getCategories(searchParams)
    }
}
