package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria

interface CmsBaseRepository<T> {

    suspend fun countAllByCriteria(criteria: Criteria): Long

    fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<T>

    fun getFixItems(): Flow<T>
}
