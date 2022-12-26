package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

@NoRepositoryBean
interface CmsCommonRepository<T> : CoroutineSortingRepository<T, String> {

    suspend fun countAllByCriteria(criteria: Criteria): Long

    fun findAllByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<T>

    override suspend fun findById(id: String): T?

    fun getFixItems(): Flow<T>
}
