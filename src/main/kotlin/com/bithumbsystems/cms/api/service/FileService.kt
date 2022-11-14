package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import org.springframework.stereotype.Service

@Service
class FileService {
    suspend fun upload() = executeIn {
        TODO("Not yet implemented")
    }

    suspend fun download() = executeIn {
        TODO("Not yet implemented")
    }
}
