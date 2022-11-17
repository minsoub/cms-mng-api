package com.bithumbsystems.cms.api.config.aws

import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.bithumbsystems.cms.api.config.mongo.MongoProperties
import com.bithumbsystems.cms.api.config.redis.RedisProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

@Configuration
class ParameterStoreConfig(
    awsProperties: AwsProperties,
    parameterStoreProperties: ParameterStoreProperties,
    clientBuilder: ClientBuilder,
    localMongoProperties: MongoProperties,
    localRedisProperties: RedisProperties,
    @Value("\${spring.profiles.active}") profile: String
) {
    private val ssmClient = clientBuilder.buildSsm(awsProperties)
    private val isLocalOrDefault = profile == "local" || profile == "default" || profile == "test"
    private val profileName = if (isLocalOrDefault) "local" else awsProperties.profileName

    val mongoProperties = if (isLocalOrDefault) localMongoProperties else MongoProperties(
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.docName,
            ParameterStoreCode.DB_URL.value
        ),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.docName,
            ParameterStoreCode.DB_USER.value
        ),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.docName,
            ParameterStoreCode.DB_PASSWORD.value
        ),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.docName,
            ParameterStoreCode.DB_PORT.value
        ),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.docName,
            ParameterStoreCode.DB_NAME.value
        )
    )

    val redisProperties = if (isLocalOrDefault) localRedisProperties else RedisProperties(
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.redisName,
            ParameterStoreCode.REDIS_HOST.value
        ),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.redisName,
            ParameterStoreCode.REDIS_PORT.value
        ).toInt(),
        getParameterValue(
            parameterStoreProperties.prefix,
            parameterStoreProperties.redisName,
            ParameterStoreCode.REDIS_TOKEN.value
        )
    )

    init {
        awsProperties.kmsKey =
            getParameterValue(
                parameterStoreProperties.smartPrefix,
                parameterStoreProperties.kmsName,
                ParameterStoreCode.KMS_ALIAS_NAME.value
            )
        awsProperties.saltKey =
            getParameterValue(
                parameterStoreProperties.smartPrefix,
                parameterStoreProperties.saltName,
                ParameterStoreCode.KMS_ALIAS_NAME.value
            )
        awsProperties.ivKey =
            getParameterValue(
                parameterStoreProperties.smartPrefix,
                parameterStoreProperties.ivName,
                ParameterStoreCode.KMS_ALIAS_NAME.value
            )
        awsProperties.jwtSecretKey =
            getParameterValue(
                parameterStoreProperties.smartPrefix,
                parameterStoreProperties.authName,
                ParameterStoreCode.JWT_SECRET_KEY.value
            )
        awsProperties.cryptoKey =
            getParameterValue(
                parameterStoreProperties.smartPrefix,
                parameterStoreProperties.cryptoName,
                ParameterStoreCode.CRYPTO_KEY.value
            )
    }

    private final fun getParameterValue(
        prefix: String,
        storeName: String,
        type: String
    ): String = ssmClient.getParameter(
        GetParameterRequest.builder().name("$prefix/${storeName}_$profileName/$type").withDecryption(true).build()
    ).parameter().value()
}
