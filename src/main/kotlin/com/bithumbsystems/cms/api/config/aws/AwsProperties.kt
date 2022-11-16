package com.bithumbsystems.cms.api.config.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
class AwsProperties(
    @Value("\${cloud.aws.credentials.profile-name}") val profileName: String,
    @Value("\${cloud.aws.s3.bucket}") val bucket: String,
    @Value("\${cloud.aws.region.static}") val region: String,
    @Value("\${cloud.aws.ssm.endpoint}") val ssmEndPoint: String,
    @Value("\${cloud.aws.kms.endpoint}") val kmsEndPoint: String,
    @Value("\${cloud.aws.sqs.endpoint}") val sqsEndPoint: String,
    @Value("\${cloud.aws.sqs.program.queue-name}") val sqsProgramQueueName: String
) {
    lateinit var kmsKey: String
    lateinit var saltKey: String
    lateinit var ivKey: String
    lateinit var jwtSecretKey: String
    lateinit var cryptoKey: String
}

@ConstructorBinding
@ConfigurationProperties(prefix = "cloud.aws.param-store")
class ParameterStoreProperties(
    val prefix: String,
    val smartPrefix: String,
    val docName: String,
    val kmsName: String,
    val saltName: String,
    val ivName: String,
    val authName: String,
    val cryptoName: String,
    val redisName: String
)
