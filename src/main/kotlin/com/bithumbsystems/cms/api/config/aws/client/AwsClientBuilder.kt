package com.bithumbsystems.cms.api.config.aws.client

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.ssm.SsmClient

interface AwsClientBuilder {
    fun buildSsm(awsProperties: AwsProperties): SsmClient
    fun buildS3(awsProperties: AwsProperties): S3AsyncClient
    fun buildKms(awsProperties: AwsProperties): KmsAsyncClient
}
