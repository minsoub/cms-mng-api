package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow

interface CmsDraftRepository<T> {

    fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<T>
}
