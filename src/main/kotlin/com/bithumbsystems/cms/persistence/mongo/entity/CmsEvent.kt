package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.model.enums.EventTarget
import com.bithumbsystems.cms.api.model.enums.EventType
import com.bithumbsystems.cms.api.model.request.Message
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_event")
class CmsEvent(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var title: String,
    var content: String,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var isFixTop: Boolean = false
    var isShow: Boolean = false
    var isDelete: Boolean = false
    var fileId: String? = null
    var shareTitle: String? = null
    var shareDescription: String? = null
    var shareFileId: String? = null
    var shareButtonName: String? = null
    var isSchedule: Boolean = false
    var scheduleDate: LocalDateTime? = null
    var isDraft: Boolean = false
    var readCount: Long = 0
    var type: EventType = EventType.DEFAULT
    var target: EventTarget? = null
    var eventStartDate: LocalDateTime? = null
    var eventEndDate: LocalDateTime? = null
    var agreementContent: String? = null
    var buttonName: String? = null
    var buttonColor: String? = null
    var buttonUrl: String? = null
    var message: Message? = null
    var isUseUpdateDate: Boolean = false
    var isAlignTop: Boolean = false
    var screenDate: LocalDateTime? = null
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
}
