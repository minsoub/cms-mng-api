package com.bithumbsystems.cms.api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.swagger.v3.core.jackson.ModelResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class WebFluxConfig : WebFluxConfigurer {

    companion object {
        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper()!!))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper()!!))
    }

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper? {
        val module = JavaTimeModule()
        val localDateTimeSerializer = LocalDateTimeSerializer(
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        )
        val localDateTimeDeserializer = LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
        val objectMapper = ObjectMapper()
        val simpleModule = SimpleModule()
        val stringDeserializer = StringDeserializer()

        module.addSerializer(LocalDateTime::class.java, localDateTimeSerializer)
        module.addDeserializer(LocalDateTime::class.java, localDateTimeDeserializer)
        simpleModule.addDeserializer(String::class.java, stringDeserializer)
        objectMapper.registerModule(module)
        objectMapper.registerModule(simpleModule)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        return objectMapper
    }

    @Bean
    fun modelResolver(objectMapper: ObjectMapper?): ModelResolver? {
        return ModelResolver(objectMapper)
    }
}
