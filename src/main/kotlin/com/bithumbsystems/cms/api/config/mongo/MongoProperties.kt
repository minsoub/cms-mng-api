package com.bithumbsystems.cms.api.config.mongo

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class MongoProperties(
    @Value("\${spring.data.mongodb.uri}") val mongodbUri: String,
    @Value("\${spring.data.mongodb.username}") val mongodbUser: String,
    @Value("\${spring.data.mongodb.password}") val mongodbPassword: String,
    @Value("\${spring.data.mongodb.port}") val mongodbPort: String,
    @Value("\${spring.data.mongodb.database}") val mongodbName: String
)
