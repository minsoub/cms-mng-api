package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildQuery
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.mongodb.BasicDBList
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria

internal class QueryUtilTest {

    @Test
    fun `검색 유틸 테스트`() {
        val searchParamsPage = SearchParams(page = 0)
        val searchParamsPageDBList = searchParamsPage.buildCriteria(isFixTop = true, isDelete = false).criteriaObject["\$and"] as BasicDBList

        val searchParamsQuery = SearchParams(query = "1")
        val searchParamsQueryDBList = searchParamsQuery.buildCriteria(isFixTop = null, isDelete = false).criteriaObject["\$or"] as BasicDBList

        val searchParamsCategory = SearchParams(categoryIds = "abcdef")
        val searchParamsCategoryDBList = searchParamsCategory.buildCriteria(isFixTop = null, isDelete = false).criteriaObject["\$and"] as BasicDBList

        val searchParamsIsBanner = SearchParams(isBanner = true)
        val searchParamsIsBannerDBList = searchParamsIsBanner.buildCriteria(isFixTop = null, isDelete = false).criteriaObject["\$and"] as BasicDBList

        val searchParamsIsShow = SearchParams(isShow = true)
        val searchParamsIsShowDBList = searchParamsIsShow.buildCriteria(isFixTop = null, isDelete = false).criteriaObject["\$and"] as BasicDBList

        val searchParamsIsUse = SearchParams(isUse = true)
        val searchParamsIsUseDBList = searchParamsIsUse.buildCriteria(isFixTop = null, isDelete = false).criteriaObject["\$and"] as BasicDBList

        searchParamsPage.buildCriteria(isFixTop = null, isDelete = null) `should be equal to` Criteria()
        (searchParamsPageDBList[0] as Document)["is_fix_top"] `should be` (true)
        (searchParamsQueryDBList[0] as Document)["title"].toString() `should be equal to` ".*1.*"
        (searchParamsQueryDBList[1] as Document)["content"].toString() `should be equal to` ".*1.*"
        (searchParamsQueryDBList[2] as Document)["name"].toString() `should be equal to` ".*1.*"
        ((searchParamsCategoryDBList[0] as Document)["category_ids"] as Document)["\$in"].toString() `should be equal to` "[abcdef]"
        (searchParamsIsBannerDBList[0] as Document)["is_banner"] `should be` (true)
        (searchParamsIsShowDBList[0] as Document)["is_show"] `should be` (true)
        (searchParamsIsUseDBList[0] as Document)["is_use"] `should be` (true)
    }

    @Test
    fun `정렬 유틸 테스트`() {
        val default: Sort = SearchParams().buildSort()

        val screenDateAsc: Sort = SearchParams(sortBy = SortBy.SCREEN_DATE, sortDirection = SortDirection.ASC).buildSort()

        val nameDesc: Sort = SearchParams(sortBy = SortBy.NAME, sortDirection = SortDirection.DESC).buildSort()

        val createDateDesc: Sort = SearchParams(sortBy = SortBy.CREATE_DATE, sortDirection = SortDirection.DESC).buildSort()

        val updateDateAsc: Sort = SearchParams(sortBy = SortBy.UPDATE_DATE, sortDirection = SortDirection.ASC).buildSort()

        val createAccountEmailAsc: Sort = SearchParams(sortBy = SortBy.CREATE_ACCOUNT_EMAIL, sortDirection = SortDirection.ASC).buildSort()

        val updateAccountEmailDesc: Sort = SearchParams(sortBy = SortBy.UPDATE_ACCOUNT_EMAIL, sortDirection = SortDirection.DESC).buildSort()

        val titleAsc: Sort = SearchParams(sortBy = SortBy.TITLE, sortDirection = SortDirection.ASC).buildSort()

        val etc: Sort = buildSort(sortBy = SortBy.TITLE, sortDirection = SortDirection.DESC)

        default.toString() `should be equal to` "screen_date: DESC,create_date: DESC"
        screenDateAsc.toString() `should be equal to` "screen_date: ASC"
        nameDesc.toString() `should be equal to` "name: DESC"
        createDateDesc.toString() `should be equal to` "create_date: DESC"
        updateDateAsc.toString() `should be equal to` "update_date: ASC"
        createAccountEmailAsc.toString() `should be equal to` "create_account_email: ASC"
        updateAccountEmailDesc.toString() `should be equal to` "update_account_email: DESC"
        titleAsc.toString() `should be equal to` "title: ASC"
        etc.toString() `should be equal to` "title: DESC"
    }

    @Test
    fun `쿼리 생성 테스트`() {
        val searchParams = SearchParams()

        val query = searchParams.page?.let { searchParams.pageSize?.let { size -> PageRequest.of(it, size) } }
            ?.let { buildQuery(criteria = SearchParams(page = 0).buildCriteria(isFixTop = true), pageable = it, sort = null) }

        query.toString() `should be equal to` "Query: { \"\$and\" : [{ \"is_fix_top\" : true}, { \"is_delete\" : false}]}, Fields: {}, Sort: {}"
    }
}
