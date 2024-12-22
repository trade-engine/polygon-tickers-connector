package com.tradeEngine.polygonTickersConnector.application.configuration

import io.polygon.kotlin.sdk.websocket.Feed
import io.polygon.kotlin.sdk.websocket.Market
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketClient
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal open class Configuration {

    @Bean
    open fun polygonWebSocketClient(
        applicationConfiguration: ApplicationConfiguration,
        listener: PolygonWebSocketListener
    ): PolygonWebSocketClient = PolygonWebSocketClient(
        apiKey = applicationConfiguration.api.key,
        feed = Feed.Delayed,
        market = Market.Stocks,
        listener = listener
    )

}