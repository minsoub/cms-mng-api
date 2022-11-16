package com.bithumbsystems.cms.api.config.operator

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.bithumbsystems.cms.api.config.web.ApplicationProperties
import com.bithumbsystems.cms.api.util.Logger
import com.bithumbsystems.cms.persistence.mongo.entity.Program
import com.bithumbsystems.cms.persistence.mongo.enums.ActionMethod
import com.bithumbsystems.cms.persistence.mongo.enums.RoleType
import com.google.gson.Gson
import io.swagger.v3.oas.annotations.Operation
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.RequestMappingInfo
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import java.util.*

@Component
class AutoGeneratorProgramConfig(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
    private val applicationProperties: ApplicationProperties,
    private val clientBuilder: ClientBuilder,
    private val awsProperties: AwsProperties
) {
    private val logger by Logger()

    @EventListener(ContextRefreshedEvent::class)
    fun start() {
        logger.debug(">> EventListener ContextRefreshedEvent start >>")
        logger.info(requestMappingHandlerMapping.handlerMethods.entries.toString())

        requestMappingHandlerMapping.handlerMethods.entries
            .filter { (_, value): Map.Entry<RequestMappingInfo, HandlerMethod> ->
                logger.info(value.toString())
                value.method.isAnnotationPresent(Operation::class.java)
            }.filter { (key): Map.Entry<RequestMappingInfo, HandlerMethod> ->
                val url = key.patternsCondition.patterns.iterator().next().patternString
                logger.debug(">> url => {} << ", url)
                !url.contains("api-docs") && !url.contains("swagger-ui")
            }.map { (key, value): Map.Entry<RequestMappingInfo, HandlerMethod> ->
                val operation = value.method.getAnnotation(Operation::class.java)
                logger.debug(">> operation : {}", operation)
                val program = Program(
                    name = operation.summary,
                    type = RoleType.valueOf(applicationProperties.roleType),
                    kindName = operation.tags[0],
                    actionMethod = ActionMethod.valueOf(key.methodsCondition.methods.iterator().next().name),
                    actionUrl = key.patternsCondition.patterns.iterator().next().patternString,
                    isUse = true,
                    description = operation.description,
                    siteId = applicationProperties.siteId
                )

                val amazonSQSAsync = clientBuilder.buildSqs(awsProperties)
                val sendMessageRequest = SendMessageRequest(
                    "${awsProperties.sqsEndPoint}/${awsProperties.sqsProgramQueueName}",
                    Gson().toJson(program)
                ).withMessageGroupId(program.siteId).withMessageDeduplicationId(UUID.randomUUID().toString())

                amazonSQSAsync.sendMessage(sendMessageRequest)
            }
    }
}
