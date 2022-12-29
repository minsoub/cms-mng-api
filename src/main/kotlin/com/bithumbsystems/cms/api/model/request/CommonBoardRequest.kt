package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.config.resolver.Account
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import javax.validation.constraints.Future
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema(description = "공통 게시판 요청")
open class CommonBoardRequest(
    @Schema(description = "제목", example = "제목", required = true, maxLength = 100)
    @field:Size(max = 100)
    open val title: String,
    @Schema(description = "본문", example = "본문", required = true)
    @field:NotBlank
    open val content: String
) {
    @Schema(description = "첨부파일 아이디", example = "59f07bfd7490409b99c00b13bb50372e", hidden = true)
    var fileId: String? = null

    @Schema(description = "공유 태그 이미지 파일 아이디", hidden = true)
    var shareFileId: String? = null

    @Schema(description = "상단 고정 여부", allowableValues = ["true", "false"], defaultValue = "false")
    var isFixTop: Boolean = false

    @Schema(description = "상태(공개 여부)", allowableValues = ["true", "false"], defaultValue = "false")
    var isShow: Boolean = false

    @Schema(description = "삭제 여부", allowableValues = ["true", "false"], defaultValue = "false")
    var isDelete: Boolean = false

    @Schema(description = "공유 태그 타이틀", example = "공유 태그 타이틀", maxLength = 50)
    @field:Size(max = 50)
    var shareTitle: String? = null

    @Schema(description = "공유 태그 설명", example = "공유 태그 설명", maxLength = 100)
    @field:Size(max = 100)
    var shareDescription: String? = null

    @Schema(description = "공유 태그 버튼명", example = "공유 태그 버튼명", maxLength = 10)
    @field:Size(max = 10)
    var shareButtonName: String? = null

    @Schema(description = "게시 예약 여부", allowableValues = ["true", "false"], defaultValue = "false")
    var isSchedule: Boolean = false

    @Schema(description = "게시 예약 일시", example = "2023-01-13 17:09:11")
    @field:Future
    var scheduleDate: LocalDateTime? = null

    @Schema(description = "초안 여부", allowableValues = ["true", "false"], defaultValue = "false")
    var isDraft: Boolean = false

    @Schema(description = "조회수", example = "0", required = false)
    var readCount: Long = 0

    @Schema(description = "날짜 업데이트 여부(등록 일시를 업데이트 일시 기준으로 노출)", allowableValues = ["true", "false"], defaultValue = "false")
    var isUseUpdateDate: Boolean = false

    @Schema(description = "게시물 상단 노출 여부(고정을 제외한 목록 최상위로 끌어올린다)", allowableValues = ["true", "false"], defaultValue = "false")
    var isAlignTop: Boolean = false

    @Schema(description = "화면 표시용 일시", hidden = true)
    var screenDate: LocalDateTime? = null

    @Schema(description = "생성자 아이디", hidden = true)
    var createAccountId: String = ""

    @Schema(description = "생성자 이메일", hidden = true)
    var createAccountEmail: String = ""

    @Schema(description = "수정자 아이디", hidden = true)
    var updateAccountId: String? = null

    @Schema(description = "수정자 이메일", hidden = true)
    var updateAccountEmail: String? = null

    @Schema(description = "수정일시, 예시: 2022-12-07 11:11:11", hidden = true)
    var updateDate: LocalDateTime? = null

    fun validate(): Boolean {
        return when {
            title.length > 100 || content.isBlank() || (shareTitle?.length ?: 0) > 50 || (shareDescription?.length ?: 0) > 100 ||
                (shareButtonName?.length ?: 0) > 10 || (scheduleDate?.isBefore(LocalDateTime.now()) == true) -> {
                false
            }

            else -> {
                true
            }
        }
    }
}

fun CommonBoardRequest.setCreateInfo(account: Account) {
    createAccountEmail = account.email
    createAccountId = account.accountId
}

fun CommonBoardRequest.setCreateInfo(fileRequest: FileRequest, account: Account) {
    fileId = fileRequest.fileKey
    shareFileId = fileRequest.shareFileKey
    createAccountEmail = account.email
    createAccountId = account.accountId
}
