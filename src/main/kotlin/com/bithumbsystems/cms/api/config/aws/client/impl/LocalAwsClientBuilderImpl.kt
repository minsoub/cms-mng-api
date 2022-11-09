package com.bithumbsystems.cms.api.config.aws.client.impl

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.aws.client.AwsClientBuilder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.ssm.SsmClient
import java.net.URI

@Component
@Profile(value = ["local", "default"])
class LocalAwsClientBuilderImpl : AwsClientBuilder {
    override fun buildSsm(awsProperties: AwsProperties): SsmClient =
        SsmClient.builder().credentialsProvider(
            ProfileCredentialsProvider.create(awsProperties.profileName)
        ).endpointOverride(URI.create(awsProperties.ssmEndPoint)).region(
            Region.of(awsProperties.region)
        ).build()

    override fun buildS3(awsProperties: AwsProperties): S3AsyncClient =
        S3AsyncClient.builder().region(Region.of(awsProperties.region))
            .credentialsProvider(ProfileCredentialsProvider.create(awsProperties.profileName))
            .build()

    override fun buildKms(awsProperties: AwsProperties): KmsAsyncClient =
        KmsAsyncClient.builder().region(Region.of(awsProperties.region))
            .endpointOverride(URI.create(awsProperties.kmsEndPoint))
            .credentialsProvider(ProfileCredentialsProvider.create(awsProperties.profileName))
            .build()
}
