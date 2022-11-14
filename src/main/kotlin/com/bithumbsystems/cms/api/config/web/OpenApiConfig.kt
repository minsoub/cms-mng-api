package com.bithumbsystems.cms.api.config.web

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI? {
        val authHeader = mapOf(
            "bearAuth" to
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .`in`(SecurityScheme.In.HEADER)
                    .name("Authorization")
        )

        val schemaRequirement = SecurityRequirement().addList("bearAuth")
        return OpenAPI().components(
            Components()
                .securitySchemes(authHeader)
                .addParameters(
                    "user_ip",
                    HeaderParameter().required(false).name("user_ip").description("user_ip").schema(StringSchema())
                )
                .addParameters(
                    "site_id",
                    HeaderParameter().required(false).name("site_id").description("site_id").schema(StringSchema())
                )
                .addParameters(
                    "my_site_id",
                    HeaderParameter().required(false).name("my_site_id").description("my_site_id").schema(StringSchema())
                )
        ).security(listOf(schemaRequirement))
    }

    @Bean
    fun customerGlobalHeaderOpenApiCustomiser(): OpenApiCustomiser? {
        return OpenApiCustomiser { openApi: OpenAPI ->
            openApi.paths.values.forEach { pathItem: PathItem ->
                pathItem.readOperations().forEach { operation: Operation ->
                    operation.addParametersItem(HeaderParameter().`$ref`("#/components/parameters/user_ip"))
                    operation.addParametersItem(HeaderParameter().`$ref`("#/components/parameters/site_id"))
                    operation.addParametersItem(HeaderParameter().`$ref`("#/components/parameters/my_site_id"))
                }
            }
        }
    }
}
