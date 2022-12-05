package com.bithumbsystems.cms.api.model.enums

enum class SortBy(val value: String) {
    DEFAULT("screen_date,create_date"),
    SCREEN_DATE("screen_date"),
    CREATE_DATE("create_date"),
    UPDATE_DATE("update_date"),
    NAME("name"),
    TITLE("title"),
    CREATE_ACCOUNT_EMAIL("create_account_email"),
    UPDATE_ACCOUNT_EMAIL("update_account_email"),
}
