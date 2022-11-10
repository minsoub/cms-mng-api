package com.bithumbsystems.cms.api.config.mongo

import com.bithumbsystems.cms.api.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories("com.bithumbsystems.cms.persistence.mongo")
class MongoConfig(
    val parameterStoreConfig: ParameterStoreConfig,
    val clientBuilder: ClientBuilder
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName() = parameterStoreConfig.mongoProperties.mongodbName

    override fun reactiveMongoClient(): MongoClient = mongoClient()

    @Bean
    fun mongoClient(): MongoClient = clientBuilder.buildMongo(configureClientSettings())

    @Bean
    override fun reactiveMongoTemplate(
        databaseFactory: ReactiveMongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): ReactiveMongoTemplate = ReactiveMongoTemplate(mongoClient(), databaseName)

    protected fun configureClientSettings(): MongoClientSettings =
        MongoClientSettings.builder()
            .streamFactoryFactory(NettyStreamFactoryFactory.builder().build())
            .applyConnectionString(getConnectionString(parameterStoreConfig.mongoProperties))
            .build()

    private fun getConnectionString(mongoProperties: MongoProperties): ConnectionString =
        ConnectionString(
            String.format(
                "mongodb://%s:%s@%s:%s",
                mongoProperties.mongodbUser,
                mongoProperties.mongodbPassword,
                mongoProperties.mongodbUrl,
                mongoProperties.mongodbPort
            )
        )
}
