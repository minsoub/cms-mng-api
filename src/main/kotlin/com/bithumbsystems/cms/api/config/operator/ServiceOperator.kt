package com.bithumbsystems.cms.api.config.operator

import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.ResponseCode
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.Response
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.recover
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

object ServiceOperator {

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

    private fun errorHandler(throwable: Throwable): ErrorData = when (throwable) {
        is IllegalArgumentException ->
            ErrorData(
                ErrorCode.ILLEGAL_ARGUMENT,
                message = ErrorCode.ILLEGAL_ARGUMENT.message
            )

        is IllegalStateException ->
            ErrorData(
                ErrorCode.ILLEGAL_STATE,
                message = ErrorCode.ILLEGAL_STATE.message
            )

        else -> {
            ErrorData(
                ErrorCode.UNKNOWN,
                message = ErrorCode.UNKNOWN.message
            )
        }
    }

    suspend fun <T> execute(
        block: suspend () -> Result<T?, ErrorData>
    ): Response<Any> = withContext(
        asContextElement(coroutineContext[ReactorContext]?.context?.get<String>(CONTEXT_NAME)!!)
    ) {
        set(kotlin.coroutines.coroutineContext[ReactorContext]?.context?.get<String>(CONTEXT_NAME)!!)
        val result = block()
        clear()
        result.fold(
            success = { Response(result = ResponseCode.SUCCESS, data = it) },
            failure = { Response(result = ResponseCode.ERROR, data = it) }
        )
    }

    suspend fun <T> executeIn(
        validator: suspend () -> Boolean,
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        check(validator())
        action()
    }.mapError {
        errorHandler(it)
    }

    suspend fun <T> executeIn(
        action: suspend () -> T?
    ): Result<T?, ErrorData> = runSuspendCatching {
        action()
    }.mapError {
        errorHandler(it)
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
        errorHandler(it)
    }
}
