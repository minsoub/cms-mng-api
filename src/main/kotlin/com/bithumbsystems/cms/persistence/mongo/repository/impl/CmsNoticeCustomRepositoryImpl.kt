package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil.buildAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import com.bithumbsystems.cms.persistence.mongo.repository.CmsNoticeCustomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class CmsNoticeCustomRepositoryImpl(
    protected val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsNoticeCustomRepository {
    override fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<CmsNotice> {
        val lookUpOperation: LookupOperation =
            LookupOperation.newLookup()
                .from("cms_notice_category")
                .localField("category_id")
                .foreignField("_id")
                .`as`("category_name")
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
