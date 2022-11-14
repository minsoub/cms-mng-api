package com.bithumbsystems.cms.api.config.web

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(
    private val awsProperties: AwsProperties
) {

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        val secretKey = SecretKeySpec(awsProperties.jwtSecretKey.toByteArray(), MacAlgorithm.HS512.getName())
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS512)
            .build()
    }

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
            .authorizeExchange { exchanges: AuthorizeExchangeSpec ->
                exchanges
                    .anyExchange().permitAll()
            }
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable().build()
    }
}
