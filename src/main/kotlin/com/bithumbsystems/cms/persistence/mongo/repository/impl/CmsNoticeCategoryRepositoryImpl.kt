package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildQuery
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import com.bithumbsystems.cms.persistence.mongo.repository.CmsBaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class CmsNoticeCategoryRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsBaseRepository<CmsNoticeCategory> {
    override suspend fun countByCriteria(criteria: Criteria): Long =
        reactiveMongoTemplate.count(Query.query(criteria), CmsNoticeCategory::class.java).awaitSingle()

    override fun findByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsNoticeCategory> =
        reactiveMongoTemplate.find(buildQuery(criteria, pageable, sort), CmsNoticeCategory::class.java)
            .asFlow()

    override fun getFixItems(): Flow<CmsNoticeCategory> {
        return reactiveMongoTemplate.aggregate(
            buildFixAggregation(lookUpOperation = null),
            "cms_notice_category",
            CmsNoticeCategory::class.java
        ).asFlow()
    }
}
