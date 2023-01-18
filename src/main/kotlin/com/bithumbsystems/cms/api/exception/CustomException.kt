package com.bithumbsystems.cms.api.exception

import com.bithumbsystems.cms.api.model.enums.ErrorCode

class CustomException(
    val code: ErrorCode,
    override val message: String
) : RuntimeException(message)
