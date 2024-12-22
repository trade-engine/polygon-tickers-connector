package com.tradeEngine.polygonTickersConnector.application.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
internal open class KafkaTopicsConfiguration {

    @Bean
    open fun minuteAggregatesTopic(): NewTopic =
        TopicBuilder
            .name("polygon-tickers-connector.minute-aggregates")
            .partitions(100)
            .replicas(3)
            .build()

}