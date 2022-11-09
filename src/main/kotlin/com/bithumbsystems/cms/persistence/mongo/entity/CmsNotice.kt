package com.bithumbsystems.cms.persistence.mongo.entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Document(collection = "cms_notice")
class CmsNotice(
    val categoryId: String,
    val title: String,
    val content: String,
    val shareTitle: String,
    val shareDescription: String,
    val scheduleDate: Long? = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
    val createAccountId: String,
    val createDate: Long
) {
    @MongoId(targetType = FieldType.STRING)
    var id: String? = UUID.randomUUID().toString().replace("-", "")
    var fileId: String? = null
    var isFixTop: Boolean = false
    var isShow: Boolean = true
    var isBanner: Boolean = false
    var isDraft: Boolean = false
    var readCount: Long = 0
    var updateAccountId: String? = null
    var updateDate: Long? = 0
}
