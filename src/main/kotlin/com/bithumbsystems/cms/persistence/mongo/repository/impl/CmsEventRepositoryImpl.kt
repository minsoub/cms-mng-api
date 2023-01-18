package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil
import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEvent
import com.bithumbsystems.cms.persistence.mongo.repository.CmsBaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class CmsEventRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsBaseRepository<CmsEvent> {

    override fun findByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsEvent> =
        reactiveMongoTemplate.find(QueryUtil.buildQuery(criteria, pageable, sort), CmsEvent::class.java)
            .asFlow()

    override fun getFixItems(): Flow<CmsEvent> {
        return reactiveMongoTemplate.aggregate(
            buildFixAggregation(lookUpOperation = null),
            "cms_event",
            CmsEvent::class.java
        ).asFlow()
    }
}
