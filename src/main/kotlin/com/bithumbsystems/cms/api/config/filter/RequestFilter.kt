package com.bithumbsystems.cms.api.config.filter

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.CONTEXT_NAME
import com.bithumbsystems.cms.api.util.Logger
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*
import reactor.util.context.Context

@Component
class RequestFilter : WebFilter {

    private val logger by Logger()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestId = UUID.randomUUID().toString()
        logger.info("WebFilter : $requestId")

        return chain.filter(exchange).contextWrite {
            Context.of(CONTEXT_NAME, requestId)
        }
    }
}
