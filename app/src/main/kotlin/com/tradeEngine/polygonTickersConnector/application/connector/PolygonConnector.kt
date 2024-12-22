package com.tradeEngine.polygonTickersConnector.application.connector

import io.polygon.kotlin.sdk.websocket.PolygonWebSocketChannel
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketClient
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketSubscription
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal class PolygonConnector(
    private val polygonWebSocketClient: PolygonWebSocketClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        logger.info("Connecting to Polygon WebSocket API...")
        polygonWebSocketClient.connectBlocking()

        logger.info("Subscribing to Polygon WebSocket API channels...")
        polygonWebSocketClient.subscribeBlocking(
            listOf(PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.AggPerMinute, "*"))
        )
    }

    @EventListener(ContextClosedEvent::class)
    fun stop() {
        logger.info("Disconnecting from Polygon WebSocket API...")
        polygonWebSocketClient.disconnectBlocking()
    }
}