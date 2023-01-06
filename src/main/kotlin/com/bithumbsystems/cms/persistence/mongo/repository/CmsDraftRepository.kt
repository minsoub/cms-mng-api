package com.bithumbsystems.cms.persistence.mongo.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable

interface CmsDraftRepository<T> {

    fun findByIsDraftIsFalseOrderByScreenDateDesc(pageable: Pageable): Flow<T>
}
