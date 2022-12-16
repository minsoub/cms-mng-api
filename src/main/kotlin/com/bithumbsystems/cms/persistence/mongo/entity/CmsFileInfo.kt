package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_file_info")
class CmsFileInfo(
    @MongoId
    val id: String,
    val name: String,
    val size: Long,
    val extension: FileExtensionType,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
)
