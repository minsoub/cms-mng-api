package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.constants.ShareConstants.VIRTUAL_ASSET_REVIEW_REPORT_TITLE
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.ReviewReportRequest
import com.bithumbsystems.cms.api.util.EncryptionUtil.encryptAES
import com.bithumbsystems.cms.api.util.getS3Url
import com.bithumbsystems.cms.persistence.redis.entity.RedisThumbnail
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_review_report")
class CmsReviewReport(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var title: String,
    var content: String,
    val createAccountId: String,
    val createAccountEmail: String,
    var createDate: LocalDateTime = LocalDateTime.now()
) {
    var searchContent: String? = "${title.trim()} | ${content.replace("&nbsp;", "").replace("<[^>]*>".toRegex(), "")}"
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
    var thumbnailFileId: String? = null
    var thumbnailUrl: String? = null
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
}

fun CmsReviewReport.setUpdateInfo(password: String, saltKey: String, ivKey: String, account: Account) {
    isShow = when (isSchedule) {
        true -> false
        false -> isShow
    }
    updateAccountId = account.accountId
    updateAccountEmail = account.email.encryptAES(password = password, saltKey = saltKey, ivKey = ivKey)
    updateDate = LocalDateTime.now()
}

fun CmsReviewReport.setUpdateInfo(
    password: String,
    saltKey: String,
    ivKey: String,
    request: ReviewReportRequest,
    account: Account,
    fileRequest: FileRequest?
) {
    title = request.title
    isFixTop = request.isFixTop
    isShow = when (isSchedule) {
        true -> false
        false -> request.isShow
    }
    isDelete = request.isDelete
    content = request.content
    searchContent = "${title.trim()} | ${content.replace("&nbsp;", "").replace("<[^>]*>".toRegex(), "")}"
    fileId = fileRequest?.fileKey ?: request.fileId
    shareTitle = request.shareTitle ?: title
    shareDescription = request.shareDescription
    shareFileId = fileRequest?.shareFileKey ?: request.shareFileId
    shareButtonName = request.shareButtonName ?: VIRTUAL_ASSET_REVIEW_REPORT_TITLE
    isSchedule = request.isSchedule
    scheduleDate = request.scheduleDate
    isDraft = when {
        isDraft && !request.isDraft -> false
        else -> isDraft
    }
    updateAccountId = account.accountId
    updateAccountEmail = account.email.encryptAES(password = password, saltKey = saltKey, ivKey = ivKey)
    updateDate = LocalDateTime.now()
    isUseUpdateDate = request.isUseUpdateDate
    isAlignTop = request.isAlignTop
    screenDate = if (isAlignTop) LocalDateTime.now() else screenDate
    createDate = if (isUseUpdateDate) LocalDateTime.now() else createDate
    thumbnailFileId = fileRequest?.thumbnailFileKey ?: request.thumbnailFileId
    thumbnailUrl = fileRequest?.thumbnailFileKey ?: request.thumbnailUrl
}

fun CmsReviewReport.toRedisEntity(): RedisThumbnail = RedisThumbnail(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl ?: thumbnailFileId?.getS3Url(),
    createDate = createDate
)
