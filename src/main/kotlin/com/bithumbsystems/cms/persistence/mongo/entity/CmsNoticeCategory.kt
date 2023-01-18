package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.util.EncryptionUtil.encryptAES
import com.bithumbsystems.cms.persistence.redis.entity.RedisNoticeCategory
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_notice_category")
class CmsNoticeCategory(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var name: String,
    var isUse: Boolean = true,
    var isDelete: Boolean = false,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
}

fun CmsNoticeCategory.setUpdateInfo(password: String, saltKey: String, ivKey: String, account: Account) {
    updateAccountId = account.accountId
    updateAccountEmail = account.email.encryptAES(password = password, saltKey = saltKey, ivKey = ivKey)
    updateDate = LocalDateTime.now()
}

fun CmsNoticeCategory.setUpdateInfo(password: String, saltKey: String, ivKey: String, request: NoticeCategoryRequest, account: Account) {
    name = request.name
    isUse = request.isUse
    isDelete = request.isDelete
    updateAccountId = account.accountId
    updateAccountEmail = account.email.encryptAES(password = password, saltKey = saltKey, ivKey = ivKey)
    updateDate = LocalDateTime.now()
}

fun CmsNoticeCategory.toRedisEntity() = RedisNoticeCategory(
    id = id,
    name = name
)
