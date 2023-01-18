package com.bithumbsystems.cms.api.config.operator

import com.bithumbsystems.cms.api.exception.CustomException
import com.bithumbsystems.cms.api.exception.UploadFailedException
import com.bithumbsystems.cms.api.exception.ValidationException
import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.ResponseCode
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.Response
import com.bithumbsystems.cms.api.util.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.recover
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.http.ResponseEntity
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

object ServiceOperator {
    private val logger by Logger()
    private val requestIdThreadLocal = ThreadLocal<String>()
    const val CONTEXT_NAME = "CMS_CONTEXT"

    fun set(requestId: String) {
        requestIdThreadLocal.set(requestId)
    }

    private fun clear() {
        requestIdThreadLocal.remove()
    }

    private fun asContextElement(requestId: String): CoroutineContext {
        return requestIdThreadLocal.asContextElement(requestId)
    }

    fun errorHandler(throwable: Throwable, errorCode: ErrorCode? = ErrorCode.ILLEGAL_STATE): ErrorData =
        errorCode?.let {
            ErrorData(
                code = errorCode,
                message = errorCode.message
            )
        } ?: makeErrorData(throwable)

    fun errorHandler(throwable: Throwable): ErrorData =
        makeErrorData(throwable)

    private fun makeErrorData(throwable: Throwable): ErrorData = when (throwable) {
        is IllegalArgumentException ->
            ErrorData(
                code = ErrorCode.ILLEGAL_ARGUMENT,
                message = ErrorCode.ILLEGAL_ARGUMENT.message
            )

        is IllegalStateException ->
            ErrorData(
                code = ErrorCode.ILLEGAL_STATE,
                message = ErrorCode.ILLEGAL_STATE.message
            )

        is UploadFailedException ->
            ErrorData(
                code = ErrorCode.UPLOAD_FAIL,
                message = ErrorCode.UPLOAD_FAIL.message
            )

        is ValidationException ->
            when (throwable.code) {
                ErrorCode.DUPLICATE_SCHEDULE_DATE ->
                    ErrorData(
                        code = throwable.code,
                        message = throwable.message
                    )

                else ->
                    ErrorData(
                        code = ErrorCode.ILLEGAL_ARGUMENT,
                        message = throwable.message
                    )
            }

        is NoSuchKeyException ->
            ErrorData(
                code = ErrorCode.NOT_FOUND,
                message = ErrorCode.NOT_FOUND.message
            )

        is CustomException ->
            ErrorData(
                code = throwable.code,
                message = throwable.message
            )

        else -> {
            ErrorData(
                code = ErrorCode.UNKNOWN,
                message = ErrorCode.UNKNOWN.message
            )
        }
    }

    suspend fun <T> execute(
        block: suspend () -> Result<T?, ErrorData>
    ): ResponseEntity<Response<Any>> = withContext(
        asContextElement(coroutineContext[ReactorContext]?.context?.get<String>(CONTEXT_NAME)!!)
    ) {
        set(kotlin.coroutines.coroutineContext[ReactorContext]?.context?.get<String>(CONTEXT_NAME)!!)
        val result = block()
        clear()
        result.fold(
            success = { ResponseEntity.ok(Response(result = ResponseCode.SUCCESS, data = it)) },
            failure = { ResponseEntity.badRequest().body(Response(result = ResponseCode.ERROR, data = it)) }
        )
    }

    suspend fun <T> executeIn(
        validator: suspend () -> Boolean,
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        require(validator())
        action()
    }.mapError {
        logger.error(it.message, it)
        errorHandler(throwable = it)
    }

    suspend fun <T> executeIn(
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        action()
    }.mapError {
        logger.error(it.message, it)
        errorHandler(throwable = it)
    }

    suspend fun <T> executeIn(
        dispatcher: CoroutineDispatcher,
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        withContext(dispatcher) {
            action()
        }
    }.mapError {
        logger.error(it.message, it)
        errorHandler(throwable = it)
    }

    suspend fun <T> executeIn(
        validator: suspend () -> Boolean,
        dispatcher: CoroutineDispatcher,
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        withContext(dispatcher) {
            require(validator())
            action()
        }
    }.mapError {
        logger.error(it.message, it)
        errorHandler(throwable = it)
    }

    suspend fun <T> executeIn(
        dispatcher: CoroutineDispatcher,
        action: suspend () -> T?,
        fallback: suspend () -> T?,
        afterJob: suspend (T) -> Unit
    ): Result<T?, ErrorData> = runSuspendCatching {
        action()
    }.recover {
        val result = fallback()
        supervisorScope {
            launch(dispatcher) {
                result?.apply {
                    afterJob(result)
                }
            }
        }
        result
    }.mapError {
        errorHandler(throwable = it)
    }
}
