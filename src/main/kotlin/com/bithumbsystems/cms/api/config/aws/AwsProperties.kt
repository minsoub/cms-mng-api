package com.bithumbsystems.cms.api.config.aws

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "cloud.aws")
class AwsProperties(
    val jwtSecretKey: String
)
