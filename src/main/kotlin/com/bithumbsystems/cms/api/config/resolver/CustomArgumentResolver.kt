package com.bithumbsystems.cms.api.config.resolver

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.errorHandler
import com.bithumbsystems.cms.api.model.enums.ErrorCode
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.core.MethodParameter
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Component
class CustomArgumentResolver(
    private val reactiveJwtDecoder: ReactiveJwtDecoder
) : HandlerMethodArgumentResolver {

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val BEARER_TYPE = "Bearer"
        const val MY_SITE_ID = "my_site_id"
        const val USER_IP = "user_ip"
        const val ACCOUNT_ID = "account_id"
        const val ROLE = "ROLE"
        const val USER_ID = "user_id"
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> = mono {
        var token = ""

        exchange.request.headers[AUTHORIZATION]?.let { header ->
            header.filter {
                it.lowercase(Locale.getDefault()).startsWith(BEARER_TYPE.lowercase(Locale.getDefault()))
            }.map { value ->
                token = value.substring(BEARER_TYPE.length).trim()
            }
        }

        val mySiteId = exchange.request.headers[MY_SITE_ID]?.let { it[0] } ?: ""
        val userIp = exchange.request.headers[USER_IP]?.let { it[0] } ?: ""

        return@mono reactiveJwtDecoder.decode(token)?.awaitSingle()?.let { jwt ->

            val accountId: String? = jwt.getClaimAsString(ACCOUNT_ID)
            val roles: JSONArray? = jwt.getClaim(ROLE)
            val email: String? = jwt.getClaimAsString(USER_ID)
            if (accountId.isNullOrEmpty() || roles == null || email.isNullOrEmpty()) {
                errorHandler(IllegalStateException(ErrorCode.INVALID_TOKEN.message), ErrorCode.INVALID_TOKEN)
            } else {
                val accountRole = (0 until roles.size step 1).map {
                    roles[it].toString()
                }.toSet()
                Account(accountId = accountId, roles = accountRole, email = email, userIp = userIp, mySiteId = mySiteId)
            }
        } ?: errorHandler(IllegalStateException(ErrorCode.INVALID_TOKEN.message), ErrorCode.INVALID_TOKEN)
    }
}
