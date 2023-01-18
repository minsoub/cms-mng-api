package com.bithumbsystems.cms.api.model.response

data class BithumbResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T?
)
