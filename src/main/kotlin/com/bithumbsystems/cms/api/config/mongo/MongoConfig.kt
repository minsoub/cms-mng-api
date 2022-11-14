package com.bithumbsystems.cms.api.config.mongo

import com.bithumbsystems.cms.api.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.api.config.client.ClientBuilder
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableReactiveMongoRepositories("com.bithumbsystems.cms.persistence.mongo")
@EnableTransactionManagement
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

    private fun configureClientSettings(): MongoClientSettings =
        MongoClientSettings.builder()
            .streamFactoryFactory(NettyStreamFactoryFactory.builder().build())
            .applyConnectionString(getConnectionString(parameterStoreConfig.mongoProperties))
            .build()

    private fun getConnectionString(mongoProperties: MongoProperties): ConnectionString =
        ConnectionString(
            "mongodb://${mongoProperties.mongodbUser}:${mongoProperties.mongodbPassword}@${mongoProperties.mongodbUrl}:${mongoProperties.mongodbPort}"
        )

    @Bean
    @Primary
    override fun mappingMongoConverter(
        databaseFactory: ReactiveMongoDatabaseFactory,
        customConversions: MongoCustomConversions,
        mappingContext: MongoMappingContext
    ): MappingMongoConverter {
        mappingContext.setFieldNamingStrategy(SnakeCaseFieldNamingStrategy())
        mappingContext.isAutoIndexCreation = true
        val converter = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext)
        converter.setCustomConversions(customConversions)
        converter.setCodecRegistryProvider(databaseFactory)
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return converter
    }

    @Bean
    fun transactionManager(factory: ReactiveMongoDatabaseFactory?): ReactiveMongoTransactionManager? {
        return ReactiveMongoTransactionManager(factory!!)
    }

    @Bean
    fun transactionOperator(manager: ReactiveTransactionManager?): TransactionalOperator? {
        return TransactionalOperator.create(manager!!)
    }
}
