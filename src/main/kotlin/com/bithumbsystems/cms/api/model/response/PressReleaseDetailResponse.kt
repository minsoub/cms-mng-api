package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
import com.bithumbsystems.cms.persistence.redis.entity.RedisBoard
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "보도자료 상세 응답")
class PressReleaseDetailResponse(
    @Schema(description = "아이디")
    val id: String,
    @Schema(description = "제목", example = "제목")
    val title: String,
    @Schema(description = "본문", example = "본문")
    val content: String,
    @Schema(description = "상단 고정 여부", example = "false")
    val isFixTop: Boolean = false,
    @Schema(description = "상태(공개 여부)", example = "false")
    val isShow: Boolean = true,
    @Schema(description = "삭제 여부", example = "false")
    val isDelete: Boolean = false,
    @Schema(description = "첨부파일 아이디", example = "59f07bfd7490409b99c00b13bb50372e")
    val fileId: String? = null,
    @Schema(description = "공유 태그 타이틀", example = "공유 태그 타이틀")
    var shareTitle: String? = null,
    @Schema(description = "공유 태그 설명", example = "공유 태그 설명")
    var shareDescription: String? = null,
    @Schema(description = "공유 태그 이미지 파일 아이디", example = "75d79b14978f43f2a6ac984533918956")
    var shareFileId: String? = null,
    @Schema(description = "공유 태그 버튼명", example = "공유 태그 버튼명")
    var shareButtonName: String? = null,
    @Schema(description = "예약 게시 여부", example = "false")
    var isSchedule: Boolean = false,
    @Schema(description = "예약 게시일시", example = "2023-01-13 17:09")
    var scheduleDate: LocalDateTime? = null,
    @Schema(description = "초안 여부", example = "false")
    var isDraft: Boolean = false,
    @Schema(description = "조회수", example = "0")
    var readCount: Long = 0,
    @Schema(description = "날짜 업데이트 여부", example = "false")
    var isUseUpdateDate: Boolean = false,
    @Schema(description = "게시물 상단 노출 여부", example = "false")
    var isAlignTop: Boolean = false,
    @Schema(description = "화면 표시용 일시", example = "2022-12-07 11:11:11")
    var screenDate: LocalDateTime? = null,
    @Schema(description = "생성자 아이디", example = "bda0f8f03f8e11edb8780242ac120002")
    val createAccountId: String,
    @Schema(description = "생성자 이메일", example = "abc@example.com")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime,
    @Schema(description = "수정자 아이디", example = "bda0f8f03f8e11edb8780242ac120002")
    val updateAccountId: String? = null,
    @Schema(description = "수정자 이메일", example = "abc@example.com")
    val updateAccountEmail: String? = null,
    @Schema(description = "수정일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val updateDate: LocalDateTime? = null
)

fun PressReleaseDetailResponse.toRedisEntity(): RedisBoard = RedisBoard(
    id = id,
    title = title,
    screenDate = screenDate ?: createDate
)

/**
 * CmsPressRelease Entity를 PressReleaseDetailResponse 변환한다.
 * @return 마스킹 처리되지 않은 응답
 */
fun CmsPressRelease.toResponse(): PressReleaseDetailResponse = PressReleaseDetailResponse(
    id = id,
    title = title,
    content = content,
    isFixTop = isFixTop,
    isShow = isShow,
    isDelete = isDelete,
    fileId = fileId,
    shareTitle = shareTitle,
    shareDescription = shareDescription,
    shareFileId = shareFileId,
    shareButtonName = shareButtonName,
    isSchedule = isSchedule,
    scheduleDate = scheduleDate,
    isDraft = isDraft,
    readCount = readCount,
    isUseUpdateDate = isUseUpdateDate,
    isAlignTop = isAlignTop,
    screenDate = screenDate,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail,
    createDate = if (isUseUpdateDate) screenDate ?: createDate else createDate,
    updateAccountId = updateAccountId,
    updateAccountEmail = updateAccountEmail,
    updateDate = updateDate
)
