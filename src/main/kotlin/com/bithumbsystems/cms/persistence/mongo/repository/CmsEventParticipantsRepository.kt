package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsEventParticipants
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CmsEventParticipantsRepository : CoroutineSortingRepository<CmsEventParticipants, String> {
    fun findByEventId(eventId: String): Flow<CmsEventParticipants>
}
