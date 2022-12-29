package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.constants.ShareConstants.ECONOMIC_RESEARCH_TITLE
import com.bithumbsystems.cms.api.model.request.EconomicResearchRequest
import com.bithumbsystems.cms.persistence.redis.entity.RedisThumbnail
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_economic_research")
class CmsEconomicResearch(
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
    var isUseUpdateDate: Boolean = false
    var isAlignTop: Boolean = false
    var screenDate: LocalDateTime? = null
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
    var thumbnailFileId: String? = null
    var thumbnailUrl: String? = null
}

fun CmsEconomicResearch.setUpdateInfo(account: Account) {
    updateAccountId = account.accountId
    updateAccountEmail = account.email
    updateDate = LocalDateTime.now()
}

fun CmsEconomicResearch.setUpdateInfo(request: EconomicResearchRequest, account: Account) {
    title = request.title
    isFixTop = request.isFixTop
    isShow = request.isShow
    isDelete = request.isDelete
    content = request.content
    fileId = request.fileId
    shareTitle = request.shareTitle ?: title
    shareDescription = request.shareDescription
    shareFileId = request.shareFileId
    shareButtonName = request.shareButtonName ?: ECONOMIC_RESEARCH_TITLE
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
    thumbnailFileId = request.thumbnailFileId
    thumbnailUrl = request.thumbnailUrl
}

fun CmsEconomicResearch.toRedisEntity(): RedisThumbnail = RedisThumbnail(
    id = id,
    title = title,
    thumbnailUrl = thumbnailFileId,
    screenDate = screenDate ?: createDate
)
