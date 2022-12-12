package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.util.NullUtil.letIfAllNotNull
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

object QueryUtil {
    private fun makeRegex(searchText: String): String = ".*$searchText.*"

    /**
     * 검색 Criteria 생성
     * @param isFixTop 상단 고정 여부
     */
    fun SearchParams.buildCriteria(isFixTop: Boolean?, isDelete: Boolean? = false): Criteria {
        var orCriteriaList = listOf<Criteria>()
        val andCriteriaList = arrayListOf<Criteria>()

        query?.let {
            orCriteriaList = listOf(
                Criteria.where("title").regex(makeRegex(query!!)),
                Criteria.where("content").regex(makeRegex(query!!)),
                Criteria.where("name").regex(makeRegex(query!!))
            )
        }

        isBanner?.let {
            andCriteriaList.add(Criteria.where("is_banner").`is`(isBanner))
        }

        isShow?.let {
            andCriteriaList.add(Criteria.where("is_show").`is`(isShow))
        }

        isUse?.let {
            andCriteriaList.add(Criteria.where("is_use").`is`(isUse))
        }

        categoryId?.let {
            andCriteriaList.add(Criteria.where("category_id").`in`(categoryId))
        }

        letIfAllNotNull(startDate, endDate) {
            andCriteriaList.add(Criteria.where("create_date").gte(startDate!!))
            andCriteriaList.add(Criteria.where("create_date").lte(endDate!!))
        }

        isFixTop?.let {
            andCriteriaList.add(Criteria.where("is_fix_top").`is`(isFixTop))
        }

        isDelete?.let {
            andCriteriaList.add(Criteria.where("is_delete").`is`(isDelete))
        }

        return when {
            orCriteriaList.isEmpty() && andCriteriaList.isEmpty() -> {
                Criteria()
            }

            orCriteriaList.isEmpty() -> {
                Criteria().andOperator(andCriteriaList)
            }

            andCriteriaList.isEmpty() -> {
                Criteria().orOperator(orCriteriaList)
            }

            else -> {
                Criteria().orOperator(orCriteriaList).andOperator(andCriteriaList)
            }
        }
    }

    /**
     * Sort 생성
     */
    fun SearchParams.buildSort(): Sort {
        if (sortBy?.value?.contains(",") == true) {
            return Sort.by(Direction.fromString(sortDirection.toString()), *(sortBy?.value?.split(",")?.toTypedArray())!!)
        }
        return Sort.by(Direction.fromString(sortDirection.toString()), sortBy?.value)
    }

    /**
     * Sort 생성
     */
    fun buildSort(sortBy: SortBy, sortDirection: SortDirection): Sort {
        if (sortBy.value.contains(",")) {
            return Sort.by(Direction.fromString(sortDirection.toString()), *(sortBy.value.split(",").toTypedArray()))
        }
        return Sort.by(Direction.fromString(sortDirection.toString()), sortBy.value)
    }

    /**
     * 검색, 페이징, 정렬 쿼리 생성
     * @param criteria 검색
     * @param pageable 페이징
     * @param sort 정렬
     */
    fun buildQuery(criteria: Criteria, pageable: Pageable, sort: Sort?): Query {
        return if (sort == null) {
            Query.query(criteria).with(pageable)
        } else {
            Query.query(criteria).with(pageable).with(sort)
        }
    }
}
