package com.bithumbsystems.cms

import io.awspring.cloud.autoconfigure.messaging.SqsAutoConfiguration
import org.redisson.spring.starter.RedissonAutoConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication(
    exclude = [
        MongoAutoConfiguration::class,
        MongoReactiveAutoConfiguration::class,
        MongoDataAutoConfiguration::class,
        EmbeddedMongoAutoConfiguration::class,
        RedissonAutoConfiguration::class,
        SqsAutoConfiguration::class
    ]
)
@ConfigurationPropertiesScan(
    basePackages = ["com.bithumbsystems.cms"]
)
class CmsMngApiApplication

fun main(args: Array<String>) {
    SpringApplication(CmsMngApiApplication::class.java).run {
        this.addListeners(ApplicationPidFileWriter())
        this.run(*args)
    }
}
