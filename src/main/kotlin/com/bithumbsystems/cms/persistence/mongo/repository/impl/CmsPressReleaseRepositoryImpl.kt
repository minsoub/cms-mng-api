package com.bithumbsystems.cms.persistence.mongo.repository.impl

import com.bithumbsystems.cms.api.util.QueryUtil
import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
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
class CmsPressReleaseRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate
) : CmsCustomRepository<CmsPressRelease> {
    override suspend fun countAllByCriteria(criteria: Criteria): Long =
        reactiveMongoTemplate.count(Query.query(criteria), CmsPressRelease::class.java).awaitSingle()

    override fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsPressRelease> =
        reactiveMongoTemplate.find(QueryUtil.buildQuery(criteria, pageable, sort), CmsPressRelease::class.java)
            .asFlow()
}
