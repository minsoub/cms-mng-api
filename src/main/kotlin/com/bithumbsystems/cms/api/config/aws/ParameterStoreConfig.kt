package com.bithumbsystems.cms.api.config.aws

import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.bithumbsystems.cms.api.config.mongo.MongoProperties
import com.bithumbsystems.cms.api.config.redis.RedisProperties
import com.bithumbsystems.cms.api.util.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

@Configuration
@Primary
class ParameterStoreConfig(
    awsProperties: AwsProperties,
    parameterStoreProperties: ParameterStoreProperties,
    clientBuilder: ClientBuilder,
    localMongoProperties: MongoProperties,
    localRedisProperties: RedisProperties,
    @Value("\${spring.profiles.active}") profile: String
) {
    private val logger by Logger()

    private val ssmClient = clientBuilder.buildSsm(awsProperties)
    private val isLocalOrDefault = profile == "local" || profile == "default" || profile == "test"
    private val profileName = if (isLocalOrDefault) "local" else awsProperties.profileName
    lateinit var mongoProperties: MongoProperties
    lateinit var redisProperties: RedisProperties

    init {
        mongoProperties = if (isLocalOrDefault) localMongoProperties else MongoProperties(
            mongodbUri = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.docName, type = ParameterStoreCode.DB_URL.value
            ),
            mongodbUser = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.docName, type = ParameterStoreCode.DB_USER.value
            ),
            mongodbPassword = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.docName, type = ParameterStoreCode.DB_PASSWORD.value
            ),
            mongodbPort = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.docName, type = ParameterStoreCode.DB_PORT.value
            ),
            mongodbName = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.docName, type = ParameterStoreCode.DB_NAME.value
            )
        )

        redisProperties = if (isLocalOrDefault) localRedisProperties else RedisProperties(
            host = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.redisName, type = ParameterStoreCode.REDIS_HOST.value
            ),
            port = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.redisName, type = ParameterStoreCode.REDIS_PORT.value
            ).toInt(),
            token = getParameterValue(
                prefix = parameterStoreProperties.prefix, storeName = parameterStoreProperties.redisName, type = ParameterStoreCode.REDIS_TOKEN.value
            )
        )

        awsProperties.kmsKey = getParameterValue(
            prefix = parameterStoreProperties.smartPrefix,
            storeName = parameterStoreProperties.kmsName,
            type = ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.saltKey = getParameterValue(
            prefix = parameterStoreProperties.smartPrefix,
            storeName = parameterStoreProperties.saltName,
            type = ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.ivKey = getParameterValue(
            prefix = parameterStoreProperties.smartPrefix, storeName = parameterStoreProperties.ivName, type = ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.jwtSecretKey = getParameterValue(
            prefix = parameterStoreProperties.smartPrefix,
            storeName = parameterStoreProperties.authName,
            type = ParameterStoreCode.JWT_SECRET_KEY.value
        )
        awsProperties.cryptoKey = getParameterValue(
            prefix = parameterStoreProperties.smartPrefix, storeName = parameterStoreProperties.cryptoName, type = ParameterStoreCode.CRYPTO_KEY.value
        )
    }

    private final fun getParameterValue(
        prefix: String,
        storeName: String,
        type: String,
    ): String {
        logger.info("getParameter: $prefix/${storeName}_$profileName/$type")
        return ssmClient.getParameter(
            GetParameterRequest.builder().name("$prefix/${storeName}_$profileName/$type").withDecryption(true).build()
        ).parameter().value()
    }
}
