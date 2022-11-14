package com.bithumbsystems.cms.api.config.client.impl

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.redisson.Redisson
import org.redisson.api.RedissonReactiveClient
import org.redisson.config.Config
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
class LocalClientBuilderImpl : ClientBuilder {
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

    override fun buildMongo(mongoClientSettings: MongoClientSettings): MongoClient = MongoClients.create()

    override fun buildRedis(config: Config): RedissonReactiveClient = Redisson.create().reactive()
}
