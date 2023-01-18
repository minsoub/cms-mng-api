package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.exception.ValidationException
import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.request.CommonBoardRequest
import com.bithumbsystems.cms.api.util.Logger
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCommonRepository
import kotlinx.coroutines.flow.count

open class CmsBaseService {
    val logger by Logger()

    suspend fun <T> validateScheduleDate(request: CommonBoardRequest, repository: CmsCommonRepository<T>): Boolean {
        request.scheduleDate?.let {
            if (repository.findByScheduleDateIsBetween(it.withSecond(0), it.withSecond(59)).count() > 0) {
                throw ValidationException(ErrorCode.DUPLICATE_SCHEDULE_DATE, ErrorCode.DUPLICATE_SCHEDULE_DATE.message)
            }
        }
        return true
    }
}
