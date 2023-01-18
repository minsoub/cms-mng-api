package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil.buildFixAggregation
import com.bithumbsystems.cms.api.util.QueryUtil.buildQuery
import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
import com.bithumbsystems.cms.persistence.mongo.repository.CmsBaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class CmsPressReleaseRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsBaseRepository<CmsPressRelease> {

    override fun findByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsPressRelease> =
        reactiveMongoTemplate.find(buildQuery(criteria, pageable, sort), CmsPressRelease::class.java)
            .asFlow()

    override fun getFixItems(): Flow<CmsPressRelease> {
        return reactiveMongoTemplate.aggregate(
            buildFixAggregation(lookUpOperation = null),
            "cms_press_release",
            CmsPressRelease::class.java
        ).asFlow()
    }
}
