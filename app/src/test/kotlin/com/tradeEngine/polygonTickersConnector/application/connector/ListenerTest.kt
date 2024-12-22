package com.tradeEngine.polygonTickersConnector.application.connector

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketClient
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketMessage
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message

class ListenerTest {

    @Test
    fun `aggregate stocks message is published`() {
        val streamBridge = mockk<StreamBridge>()
        every { streamBridge.send("polygon-tickers-connector.minute-aggregates", any()) } returns true

        val polygonWebSocketClient = mockk<PolygonWebSocketClient>()

        val listener = Listener(streamBridge)

        val aggregateStocksMessage = PolygonWebSocketMessage.StocksMessage.Aggregate(
            eventType = "AM",
            ticker = "GTE",
            volume = 4110.toDouble(),
            accumulatedVolume = 9470157.toDouble(),
            officialOpenPrice = 0.4372,
            volumeWeightedAveragePrice = 0.4488,
            openPrice = 0.4488,
            closePrice = 0.4486,
            highPrice = 0.4489,
            lowPrice = 0.4486,
            averagePrice = 0.4352,
            averageTradeSize = 685.toDouble(),
            startTimestampMillis = 1610144640000,
            endTimestampMillis = 1610144700000,
        )

        val expected = aggregateStocksMessage.toAggregate(true).toMessage()

        listener.onReceive(polygonWebSocketClient, aggregateStocksMessage)

        verify(exactly = 1) {
            streamBridge.send("polygon-tickers-connector.minute-aggregates", match {
                it is Message<*>
                        && it.payload is Aggregate
                        && expected.headers[KafkaHeaders.KEY] as ByteArray contentEquals it.headers[KafkaHeaders.KEY] as ByteArray
                        && expected.payload == it.payload
            })
        }

        confirmVerified(streamBridge)
    }

    @Test
    fun `reset flag is correctly set`() {
        val streamBridge = mockk<StreamBridge>()
        every { streamBridge.send("polygon-tickers-connector.minute-aggregates", any()) } returns true

        val polygonWebSocketClient = mockk<PolygonWebSocketClient>()

        val listener = Listener(streamBridge)

        val aggregateStocksMessages = listOf(
            PolygonWebSocketMessage.StocksMessage.Aggregate(
                eventType = "AM",
                ticker = "GTE",
                volume = 4110.toDouble(),
                accumulatedVolume = 9470157.toDouble(),
                officialOpenPrice = 0.4372,
                volumeWeightedAveragePrice = 0.4488,
                openPrice = 0.4488,
                closePrice = 0.4486,
                highPrice = 0.4489,
                lowPrice = 0.4486,
                averagePrice = 0.4352,
                averageTradeSize = 685.toDouble(),
                startTimestampMillis = 1610144640000,
                endTimestampMillis = 1610144700000,
            ),
            PolygonWebSocketMessage.StocksMessage.Aggregate(
                eventType = "AM",
                ticker = "GTF",
                volume = 4110.toDouble(),
                accumulatedVolume = 9470157.toDouble(),
                officialOpenPrice = 0.4372,
                volumeWeightedAveragePrice = 0.4488,
                openPrice = 0.4488,
                closePrice = 0.4486,
                highPrice = 0.4489,
                lowPrice = 0.4486,
                averagePrice = 0.4352,
                averageTradeSize = 685.toDouble(),
                startTimestampMillis = 1610144640000,
                endTimestampMillis = 1610144700000,
            ),
            PolygonWebSocketMessage.StocksMessage.Aggregate(
                eventType = "AM",
                ticker = "GTG",
                volume = 4110.toDouble(),
                accumulatedVolume = 9470157.toDouble(),
                officialOpenPrice = 0.4372,
                volumeWeightedAveragePrice = 0.4488,
                openPrice = 0.4488,
                closePrice = 0.4486,
                highPrice = 0.4489,
                lowPrice = 0.4486,
                averagePrice = 0.4352,
                averageTradeSize = 685.toDouble(),
                startTimestampMillis = 1610144640000,
                endTimestampMillis = 1610144700000,
            ),
            PolygonWebSocketMessage.StocksMessage.Aggregate(
                eventType = "AM",
                ticker = "GTE",
                volume = 4110.toDouble(),
                accumulatedVolume = 9470157.toDouble(),
                officialOpenPrice = 0.4372,
                volumeWeightedAveragePrice = 0.4488,
                openPrice = 0.4488,
                closePrice = 0.4486,
                highPrice = 0.4489,
                lowPrice = 0.4486,
                averagePrice = 0.4352,
                averageTradeSize = 685.toDouble(),
                startTimestampMillis = 1610144700000,
                endTimestampMillis = 1610144760000,
            ),
            PolygonWebSocketMessage.StocksMessage.Aggregate(
                eventType = "AM",
                ticker = "GTH",
                volume = 4110.toDouble(),
                accumulatedVolume = 9470157.toDouble(),
                officialOpenPrice = 0.4372,
                volumeWeightedAveragePrice = 0.4488,
                openPrice = 0.4488,
                closePrice = 0.4486,
                highPrice = 0.4489,
                lowPrice = 0.4486,
                averagePrice = 0.4352,
                averageTradeSize = 685.toDouble(),
                startTimestampMillis = 1610144700000,
                endTimestampMillis = 1610144760000,
            ),
        )

        val expecteds = listOf(
            aggregateStocksMessages[0].toAggregate(true).toMessage(),
            aggregateStocksMessages[1].toAggregate(true).toMessage(),
            aggregateStocksMessages[2].toAggregate(true).toMessage(),
            aggregateStocksMessages[3].toAggregate(false).toMessage(),
            aggregateStocksMessages[4].toAggregate(true).toMessage(),
        )

        aggregateStocksMessages.forEach { listener.onReceive(polygonWebSocketClient, it) }

        verify(exactly = 1) {
            expecteds.forEach { expected ->
                streamBridge.send("polygon-tickers-connector.minute-aggregates", match {
                    it is Message<*>
                            && it.payload is Aggregate
                            && expected.headers[KafkaHeaders.KEY] as ByteArray contentEquals it.headers[KafkaHeaders.KEY] as ByteArray
                            && expected.payload == it.payload
                })
            }
        }
    }

    @Test
    fun `aggregate stocks message is correctly converted to aggregate`() {
        val aggregateStocksMessage = PolygonWebSocketMessage.StocksMessage.Aggregate(
            eventType = "AM",
            ticker = "GTE",
            volume = 4110.toDouble(),
            accumulatedVolume = 9470157.toDouble(),
            officialOpenPrice = 0.4372,
            volumeWeightedAveragePrice = 0.4488,
            openPrice = 0.4488,
            closePrice = 0.4486,
            highPrice = 0.4489,
            lowPrice = 0.4486,
            averagePrice = 0.4352,
            averageTradeSize = 685.toDouble(),
            startTimestampMillis = 1610144640000,
            endTimestampMillis = 1610144700000,
        )

        val isReset = true

        val aggregate = Aggregate(
            ticker = "GTE",
            volumeWeightedAverage = 0.4488.toBigDecimal(),
            open = 0.4488.toBigDecimal(),
            close = 0.4486.toBigDecimal(),
            high = 0.4489.toBigDecimal(),
            low = 0.4486.toBigDecimal(),
            volume = 4110,
            transactions = 6,
            windowStart = 1610144640000,
            windowEnd = 1610144699999,
            reset = isReset
        )

        val convertedAggregate = aggregateStocksMessage.toAggregate(isReset)

        assertEquals(aggregate, convertedAggregate)
    }

    @Test
    fun `aggregate is correctly converted to message`() {
        val aggregate = Aggregate(
            ticker = "GTE",
            volumeWeightedAverage = 0.4488.toBigDecimal(),
            open = 0.4488.toBigDecimal(),
            close = 0.4486.toBigDecimal(),
            high = 0.4489.toBigDecimal(),
            low = 0.4486.toBigDecimal(),
            volume = 4110,
            transactions = 6,
            windowStart = 1610144640000,
            windowEnd = 1610144699999,
            reset = true
        )

        val message = aggregate.toMessage()

        assertArrayEquals(aggregate.ticker.toByteArray(), message.headers[KafkaHeaders.KEY] as ByteArray)
        assertEquals(aggregate, message.payload)
    }
}