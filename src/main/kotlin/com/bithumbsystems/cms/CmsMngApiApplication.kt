package com.bithumbsystems.cms

import org.redisson.spring.starter.RedissonAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        MongoAutoConfiguration::class,
        MongoReactiveAutoConfiguration::class,
        MongoDataAutoConfiguration::class,
        EmbeddedMongoAutoConfiguration::class,
        RedissonAutoConfiguration::class
    ]
)
@ConfigurationPropertiesScan(
    basePackages = ["com.bithumbsystems.cms"]
)
class CmsMngApiApplication

fun main(args: Array<String>) {
    runApplication<CmsMngApiApplication>(*args)
}
