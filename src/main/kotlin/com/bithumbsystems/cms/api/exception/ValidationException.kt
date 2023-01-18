package com.bithumbsystems.cms.api.exception

import com.bithumbsystems.cms.api.model.enums.ErrorCode

class ValidationException(
    val code: ErrorCode?,
    override val message: String
) : RuntimeException(message) {
    constructor(message: String) : this(null, message)
}
