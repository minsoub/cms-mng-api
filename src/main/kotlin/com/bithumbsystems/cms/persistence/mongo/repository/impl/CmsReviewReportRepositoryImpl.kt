package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil
import com.bithumbsystems.cms.persistence.mongo.entity.CmsReviewReport
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
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
class CmsReviewReportRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsCustomRepository<CmsReviewReport> {
    override suspend fun countAllByCriteria(criteria: Criteria): Long =
        reactiveMongoTemplate.count(Query.query(criteria), CmsReviewReport::class.java).awaitSingle()

    override fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsReviewReport> =
        reactiveMongoTemplate.find(QueryUtil.buildQuery(criteria, pageable, sort), CmsReviewReport::class.java)
            .asFlow()
}
