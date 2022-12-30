package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil
import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.persistence.mongo.entity.CmsInvestWarning
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
class CmsInvestWarningRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsBaseRepository<CmsInvestWarning> {
    override suspend fun countAllByCriteria(criteria: Criteria): Long =
        reactiveMongoTemplate.count(Query.query(criteria), CmsInvestWarning::class.java).awaitSingle()

    override fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsInvestWarning> =
        reactiveMongoTemplate.find(QueryUtil.buildQuery(criteria, pageable, sort), CmsInvestWarning::class.java)
            .asFlow()

    override fun getFixItems(): Flow<CmsInvestWarning> {
        return reactiveMongoTemplate.aggregate(
            buildFixAggregation(lookUpOperation = null),
            "cms_investment_warning",
            CmsInvestWarning::class.java
        ).asFlow()
    }
}
