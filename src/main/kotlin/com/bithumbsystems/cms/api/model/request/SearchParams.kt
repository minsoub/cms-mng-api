package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

/**
 * 목록 조회시 조회 조건
 */
data class SearchParams(
    var page: Int? = 0,
    var pageSize: Int? = 15,
    var query: String? = null,
    var categoryId: String? = null,
    var isBanner: Boolean? = null,
    var isShow: Boolean? = null,
    var isUse: Boolean? = null,
    var sortBy: SortBy? = SortBy.DEFAULT,
    var sortDirection: SortDirection? = SortDirection.DESC,

    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    var startDate: LocalDate? = null,

    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    var endDate: LocalDate? = null,
)
