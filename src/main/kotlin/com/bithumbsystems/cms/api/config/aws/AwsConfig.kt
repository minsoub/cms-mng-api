package com.bithumbsystems.cms.api.config.aws

import com.bithumbsystems.cms.api.config.aws.client.AwsClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClient

@Configuration
class AwsConfig(
    private val awsProperties: AwsProperties,
    val awsClientBuilder: AwsClientBuilder
) {
    @Bean
    fun s3Client(): S3AsyncClient = awsClientBuilder.buildS3(awsProperties)

    @Bean
    fun kmsClient(): KmsAsyncClient = awsClientBuilder.buildKms(awsProperties)
}
