package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
interface CmsCustomRepository<T> {

    suspend fun countAllByCriteria(criteria: Criteria): Long

    fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<T>
}
