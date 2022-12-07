package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

/**
 * 목록 조회시 조회 조건
 */
data class SearchParams(
    @Parameter(description = "이동할 페이지")
    var page: Int? = 0,
    @Parameter(description = "페이지 사이즈")
    var pageSize: Int? = 15,
    @Parameter(description = "검색어")
    var query: String? = null,
    @Parameter(description = "카테고리 아이디")
    var categoryId: String? = null,
    @Parameter(description = "배너 여부")
    var isBanner: Boolean? = null,
    @Parameter(description = "사용 여부")
    var isShow: Boolean? = null,
    @Parameter(description = "상태(카테고리)")
    var isUse: Boolean? = null,
    @Parameter(description = "정렬 타겟")
    var sortBy: SortBy? = SortBy.DEFAULT,
    @Parameter(description = "정렬 방향")
    var sortDirection: SortDirection? = SortDirection.DESC,

    @Parameter(description = "검색 시작일")
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,

    @Parameter(description = "검색 종료일")
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null
)
