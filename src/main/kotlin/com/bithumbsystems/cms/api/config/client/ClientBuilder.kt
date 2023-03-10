package com.bithumbsystems.cms.api.config.client

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.bithumbsystems.cms.api.config.aws.AwsProperties
import org.redisson.api.RedissonReactiveClient
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.ssm.SsmClient

interface ClientBuilder {
    fun buildSsm(awsProperties: AwsProperties): SsmClient
    fun buildS3(awsProperties: AwsProperties): S3AsyncClient
    fun buildKms(awsProperties: AwsProperties): KmsAsyncClient
    fun buildRedis(config: Config): RedissonReactiveClient
    fun buildSqs(awsProperties: AwsProperties): AmazonSQSAsync

    @Bean
    fun buildWebClient(): WebClient = WebClient.create()
}
