package com.bithumbsystems.cms.persistence.mongo.entity

import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document("cms_event_participants")
@CompoundIndex(
    def = "{'event_id': 1, 'uid': 1}",
    name = "event_id_uid_idx",
    unique = true
)
class CmsEventParticipants(
    val eventId: String,
    val uid: String,
    val isAgree: Boolean = true,
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Indexed(expireAfter = "30d")
    val eventEndDate: LocalDateTime
)
