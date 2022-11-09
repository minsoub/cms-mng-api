package com.bithumbsystems.cms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(
    basePackages = ["com.bithumbsystems.cms"]
)
class CmsMngApiApplication

fun main(args: Array<String>) {
    runApplication<CmsMngApiApplication>(*args)
}
