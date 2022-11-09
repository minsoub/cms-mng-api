package com.bithumbsystems.cms.api.config.aws

import com.bithumbsystems.cms.api.config.aws.client.AwsClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

@Configuration
class ParameterStoreConfig(
    awsProperties: AwsProperties,
    parameterStoreProperties: ParameterStoreProperties,
    awsClientBuilder: AwsClientBuilder,
    @Value("\${spring.profiles.active}") profile: String
) {
    private val ssmClient = awsClientBuilder.buildSsm(awsProperties)
    private val profileName =
        if (profile == "local" || profile == "default") profile else awsProperties.profileName

    init {
        /*awsProperties.kmsKey = getParameterValue(
            parameterStoreProperties.prefix,
            ssmClient,
            parameterStoreProperties.kmsName,
            profileName,
            ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.saltKey = getParameterValue(
            parameterStoreProperties.prefix,
            ssmClient,
            parameterStoreProperties.saltName,
            profileName,
            ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.ivKey = getParameterValue(
            parameterStoreProperties.prefix,
            ssmClient,
            parameterStoreProperties.ivName,
            profileName,
            ParameterStoreCode.KMS_ALIAS_NAME.value
        )
        awsProperties.jwtSecretKey = getParameterValue(
            parameterStoreProperties.prefix,
            ssmClient,
            parameterStoreProperties.authName,
            profileName,
            ParameterStoreCode.JWT_SECRET_KEY.value
        )
        awsProperties.cryptoKey = getParameterValue(
            parameterStoreProperties.prefix,
            ssmClient,
            parameterStoreProperties.cryptoName,
            profileName,
            ParameterStoreCode.CRYPTO_KEY.value
        )*/
    }

    private final fun getParameterValue(
        prefix: String,
        ssmClient: SsmClient,
        storeName: String,
        profileName: String,
        type: String
    ): String = ssmClient.getParameter(
        GetParameterRequest.builder()
            .name(
                String.format(
                    "%s/%s_%s/%s",
                    prefix,
                    storeName,
                    profileName,
                    type
                )
            )
            .withDecryption(true)
            .build()
    ).parameter().value()
}
