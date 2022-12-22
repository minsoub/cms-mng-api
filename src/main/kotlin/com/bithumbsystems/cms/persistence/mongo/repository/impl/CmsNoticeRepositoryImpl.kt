package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil.buildAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.repository.CmsBaseRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class CmsNoticeRepositoryImpl(
    protected val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsBaseRepository<CmsNotice>, CmsDraftRepository<CmsNotice> {
    override suspend fun countAllByCriteria(criteria: Criteria): Long =
        reactiveMongoTemplate.count(Query.query(criteria), CmsNotice::class.java).awaitSingle()

    override fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsNotice> {
        val lookUpOperation: LookupOperation =
            LookupOperation.newLookup()
                .from("cms_notice_category")
                .localField("category_ids")
                .foreignField("_id")
                .`as`("category_names")

        return reactiveMongoTemplate
            .aggregate(buildAggregation(lookUpOperation, criteria, pageable, sort), "cms_notice", CmsNotice::class.java)
            .asFlow()
    }

    suspend fun findById(id: String): CmsNotice? {
        val matchOperation: MatchOperation = Aggregation.match(Criteria.where("_id").`is`(id))
        val lookUpOperation: LookupOperation =
            LookupOperation.newLookup()
                .from("cms_notice_category")
                .localField("category_ids")
                .foreignField("_id")
                .`as`("category_names")
        return reactiveMongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, lookUpOperation), "cms_notice", CmsNotice::class.java)
            .awaitFirstOrNull()
    }

    override fun getFixItems(): Flow<CmsNotice> {
        val lookUpOperation: LookupOperation =
            LookupOperation.newLookup()
                .from("cms_notice_category")
                .localField("category_ids")
                .foreignField("_id")
                .`as`("category_names")

        return reactiveMongoTemplate.aggregate(buildFixAggregation(lookUpOperation), "cms_notice", CmsNotice::class.java).asFlow()
    }

    override fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<CmsNotice> {
        val lookUpOperation: LookupOperation =
            LookupOperation.newLookup()
                .from("cms_notice_category")
                .localField("category_ids")
                .foreignField("_id")
                .`as`("category_names")
        return reactiveMongoTemplate.aggregate(
            buildAggregation(
                listOf(
                    lookUpOperation,
                    Aggregation.match(Criteria.where("is_draft").`is`(false)),
                    Aggregation.sort(buildSort()),
                    Aggregation.limit(5)
                )
            ),
            "cms_notice",
            CmsNotice::class.java
        ).asFlow()
    }
}
