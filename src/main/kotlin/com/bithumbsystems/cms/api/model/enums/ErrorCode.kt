package com.bithumbsystems.cms.api.model.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 코드")
enum class ErrorCode(val message: String) {
    UNKNOWN("Unknown Error"),
    ILLEGAL_ARGUMENT("ILLEGAL ARGUMENT"),
    ILLEGAL_STATE("ILLEGAL STATE"),
    INVALID_TOKEN("Invalid token"),
    NO_CONTENT("Content must not be blank"),
    INVALID_CATEGORY("There must be a minimum of one category and a maximum of two"),
    INVALID_TITLE("The length of the title must be less than 100 characters"),
    INVALID_SHARE_TITLE("Share tag title must be 50 characters or less"),
    INVALID_SHARE_DESCRIPTION("Share tag descriptions must be 100 characters or less"),
    INVALID_SHARE_BUTTON_NAME("Share tag button name must be 10 characters or less"),
    INVALID_SCHEDULE_DATE("Schedule date is must be after present"),
    INVALID_EVENT_DATE("Event start or end date must be after present"),
    INVALID_MESSAGE_LENGTH("Message length must be less than 20 characters"),
    INVALID_BUTTON_NAME_LENGTH("Length of the button name must be less than 10 characters"),
    INVALID_BUTTON_COLOR_LENGTH("Length of the button color must be 10 characters or less"),
    DUPLICATE_SCHEDULE_DATE("There is already a scheduled post for that time"),
    INVALID_FILE("Invalid file"),
    INVALID_FILE_FORMAT("Invalid file format"),
    INVALID_FILE_SIZE("Invalid file size"),
    UID_DECRYPT_FAIL("Uid decrypt failure"),
    UPLOAD_FAIL("File upload failure"),
    DOWNLOAD_FAIL("File download failure"),
    NOT_FOUND("No such Key found"),
}
