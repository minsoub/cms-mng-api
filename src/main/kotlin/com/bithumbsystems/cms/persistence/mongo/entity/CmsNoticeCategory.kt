package com.bithumbsystems.cms.persistence.mongo.entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_notice_category")
class CmsNoticeCategory(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    val name: String,
    val isUse: Boolean = true,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val updateAccountId: String? = null,
    val updateAccountEmail: String? = null,
    val updateDate: LocalDateTime? = null
)
