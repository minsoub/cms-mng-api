package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil.buildAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.repository.CmsBaseRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsDraftRepository
import com.bithumbsystems.cms.persistence.mongo.wrapper.LookupLetPipelineWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.Document
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.MongoExpression
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class CmsNoticeRepositoryImpl(
    protected val reactiveMongoTemplate: ReactiveMongoTemplate,
) : CmsBaseRepository<CmsNotice>, CmsDraftRepository<CmsNotice> {

    private val lookupWithPipeline = LookupLetPipelineWrapper(
        lookup = Document.parse("{\$lookup: {from: 'cms_notice_category', as: 'category_names'}}"),
        letOperation = Document.parse("{categories: '\$category_ids'}"),
        pipelineAggregation = Aggregation.newAggregation(
            Aggregation.match { AggregationExpression.from(MongoExpression.create("\$expr: { \$in: ['\$_id', '\$\$categories']}")).toDocument() },
            Aggregation.addFields().addFieldWithValue("_order", mapOf("\$indexOfArray" to arrayOf("\$\$categories", "\$_id"))).build(),
            Aggregation.sort(Sort.Direction.ASC, "_order"),
            Aggregation.project(Fields.fields("_order", "name"))
        )
    )

    override fun findByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsNotice> =
        reactiveMongoTemplate
            .aggregate(buildAggregation(lookupWithPipeline, criteria, pageable, sort), "cms_notice", CmsNotice::class.java).asFlow()

    suspend fun findById(id: String): CmsNotice? =
        reactiveMongoTemplate.aggregate(
            Aggregation.newAggregation(Aggregation.match(Criteria.where("_id").`is`(id)), lookupWithPipeline),
            "cms_notice",
            CmsNotice::class.java
        ).awaitFirstOrNull()

    override fun getFixItems(): Flow<CmsNotice> =
        reactiveMongoTemplate.aggregate(buildFixAggregation(lookupWithPipeline), "cms_notice", CmsNotice::class.java).asFlow()

    override fun findByIsDraftIsFalseOrderByScreenDateDesc(pageable: Pageable): Flow<CmsNotice> =
        reactiveMongoTemplate.aggregate(
            buildAggregation(
                listOf(
                    lookupWithPipeline,
                    Aggregation.match(Criteria.where("is_draft").`is`(false)),
                    Aggregation.sort(buildSort()),
                    Aggregation.limit(pageable.pageSize.toLong()),
                )
            ),
            "cms_notice",
            CmsNotice::class.java
        ).asFlow()
}
