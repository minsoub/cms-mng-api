package com.bithumbsystems.cms.api.config.resolver

import com.bithumbsystems.cms.api.util.GsonUtil.gson
import kotlinx.coroutines.reactor.mono
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class QueryParamArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(QueryParam::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> = mono {
        val json: String? = gson.toJson(exchange.request.queryParams.toSingleValueMap())
        gson.fromJson(json, parameter.parameterType)
    }
}
