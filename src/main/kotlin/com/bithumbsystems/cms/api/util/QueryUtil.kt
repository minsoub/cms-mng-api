package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.util.NullUtil.letIfAllNotNull
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

object QueryUtil {
    private fun makeRegex(searchText: String): String = ".*$searchText.*"

    /**
     * 검색 Criteria 생성
     * @param isFixTop 상단 고정 여부
     */
    fun SearchParams.buildCriteria(isFixTop: Boolean?, isDelete: Boolean? = false): Criteria {
        var orCriteriaList: List<Criteria> = listOf()
        val andCriteriaList: ArrayList<Criteria> = arrayListOf()

        orCriteriaList = buildCriteriaList(orCriteriaList, andCriteriaList, isFixTop, isDelete)

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

    private fun SearchParams.buildCriteriaList(
        orCriteriaList: List<Criteria>,
        andCriteriaList: ArrayList<Criteria>,
        isFixTop: Boolean?,
        isDelete: Boolean?
    ): List<Criteria> {
        var criteriaList = orCriteriaList
        query?.let {
            criteriaList = listOf(
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

        categoryIds?.let {
            andCriteriaList.add(Criteria.where("category_ids").`in`(categoryIds))
        }

        eventType?.let {
            andCriteriaList.add(Criteria.where("event_type").`is`(eventType))
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
        return criteriaList
    }

    fun buildCriteriaForDraft(id: String): Criteria {
        return Criteria.where("is_draft").`is`(true).and("create_account_id").`is`(id).and("is_delete").`is`(false)
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

    fun buildSort(): Sort {
        return Sort.by(Direction.DESC, "screen_date", "create_date")
    }

    fun buildSortForDraft(): Sort {
        return Sort.by(Direction.DESC, "is_draft", "screen_date", "create_date")
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

    fun buildFixAggregation(lookUpOperation: LookupOperation?): Aggregation {
        val matchOperation: MatchOperation = Aggregation.match(
            Criteria.where("is_fix_top").`is`(true).andOperator(Criteria.where("is_delete").`is`(false))
        )
        return if (lookUpOperation == null) {
            Aggregation.newAggregation(
                matchOperation,
                Aggregation.sort(buildSort())
            )
        } else {
            Aggregation.newAggregation(
                lookUpOperation,
                matchOperation,
                Aggregation.sort(buildSort())
            )
        }
    }

    fun buildAggregation(lookUpOperation: LookupOperation, criteria: Criteria, pageable: Pageable, sort: Sort?): Aggregation {
        val aggregation: MutableList<AggregationOperation> = mutableListOf(lookUpOperation, Aggregation.match(criteria))
        sort?.let {
            aggregation.add(Aggregation.sort(it))
        }
        if (pageable.isPaged) {
            aggregation.add(Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong()))
            aggregation.add(Aggregation.limit(pageable.pageSize.toLong()))
        }
        return Aggregation.newAggregation(aggregation)
    }

    fun buildAggregation(aggregationList: List<AggregationOperation>): Aggregation {
        return Aggregation.newAggregation(aggregationList)
    }
}

fun Criteria.withoutDraft(): Criteria = this.and("is_draft").`is`(false)
