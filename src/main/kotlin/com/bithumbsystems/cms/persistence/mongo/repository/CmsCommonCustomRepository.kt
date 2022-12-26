package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface CmsCommonCustomRepository<T> : CmsCommonRepository<T> {
    override suspend fun findById(id: String): T?

    fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<T>
}
