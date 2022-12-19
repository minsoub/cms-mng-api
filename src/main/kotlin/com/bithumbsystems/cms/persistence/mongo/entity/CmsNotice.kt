package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.aggregate.Category
import com.bithumbsystems.cms.api.model.constants.ShareConstants.NOTICE_TITLE
import com.bithumbsystems.cms.api.model.request.NoticeRequest
import com.bithumbsystems.cms.persistence.redis.entity.RedisNotice
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_notice")
class CmsNotice(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var categoryId: List<String>,
    var title: String,
    var content: String,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var isFixTop: Boolean = false
    var isShow: Boolean = false
    var isDelete: Boolean = false
    var isBanner: Boolean = false
    var fileId: String? = null
    var shareTitle: String? = null
    var shareDescription: String? = null
    var shareFileId: String? = null
    var shareButtonName: String? = null
    var isSchedule: Boolean = false
    var scheduleDate: LocalDateTime? = null
    var isDraft: Boolean = false
    var readCount: Long = 0
    var isUseUpdateDate: Boolean = false
    var isAlignTop: Boolean = false
    var screenDate: LocalDateTime? = null
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
    var categoryName: List<Category> = listOf()
}

fun CmsNotice.setUpdateInfo(account: Account) {
    updateAccountId = account.accountId
    updateAccountEmail = account.email
    updateDate = LocalDateTime.now()
}

fun CmsNotice.setUpdateInfo(request: NoticeRequest, account: Account) {
    categoryId = request.categoryId
    title = request.title
    isFixTop = request.isFixTop
    isShow = request.isShow
    isDelete = request.isDelete
    isBanner = request.isBanner
    content = request.content
    fileId = request.fileId
    shareTitle = request.shareTitle ?: title
    shareDescription = request.shareDescription
    shareFileId = request.shareFileId
    shareButtonName = request.shareButtonName ?: NOTICE_TITLE
    isSchedule = request.isSchedule
    scheduleDate = request.scheduleDate
    isDraft = request.isDraft
    readCount = request.readCount
    updateAccountId = account.accountId
    updateAccountEmail = account.email
    updateDate = LocalDateTime.now()
    isUseUpdateDate = request.isUseUpdateDate
    isAlignTop = request.isAlignTop
    screenDate = if (isUseUpdateDate) LocalDateTime.now() else null
}

fun CmsNotice.toRedisEntity(): RedisNotice = RedisNotice(
    id = id,
    title = title,
    categoryName = categoryName.map { it.name }.toList(),
    screenDate = screenDate ?: createDate
)
