package com.tradeEngine.polygonTickersConnector.application.connector

import io.polygon.kotlin.sdk.websocket.PolygonWebSocketClient
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketListener
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketMessage
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.math.round

@Component
internal class Listener(private val streamBridge: StreamBridge) : PolygonWebSocketListener {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val tickersSeenSinceStart: MutableSet<String> = mutableSetOf()

    override fun onAuthenticated(client: PolygonWebSocketClient) {
        logger.info("Authenticated with Polygon WebSocket API.")

        if (tickersSeenSinceStart.isNotEmpty()) {
            logger.warn("Found {} seen ticker(s). Clearing...", tickersSeenSinceStart.count())
            tickersSeenSinceStart.clear()
        }
    }

    override fun onDisconnect(client: PolygonWebSocketClient) {
        logger.info("Disconnected from Polygon WebSocket API.")
    }

    override fun onError(client: PolygonWebSocketClient, error: Throwable) {
        logger.error("An error occurred.", error)

        client.disconnectBlocking()
    }

    override fun onReceive(client: PolygonWebSocketClient, message: PolygonWebSocketMessage) {
        when (message) {
            is PolygonWebSocketMessage.StatusMessage -> logger.info(
                "Received status message from Polygon WebSocket API: {}.",
                message
            )

            is PolygonWebSocketMessage.StocksMessage.Aggregate -> {
                streamBridge.send(
                    "polygon-tickers-connector.minute-aggregates",
                    message.toAggregate(
                        tickersSeenSinceStart.add(
                            message.ticker!!
                        ).also { if (it) logger.debug("""Saw ticker "{}" for the first time.""", message.ticker!!) }
                    ).toMessage()
                )
            }

            else -> logger.warn("Received unhandled message from Polygon WebSocket API: {}.", message)

        }
    }
}

internal data class Aggregate(
    val ticker: String,
    val volumeWeightedAverage: BigDecimal,
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: Long,
    val transactions: Long,
    val windowStart: Long,
    val windowEnd: Long,
    val reset: Boolean
)

internal fun PolygonWebSocketMessage.StocksMessage.Aggregate.toAggregate(isReset: Boolean) = Aggregate(
    ticker = this.ticker!!,
    volumeWeightedAverage = this.volumeWeightedAveragePrice!!.toBigDecimal(),
    open = this.openPrice!!.toBigDecimal(),
    close = this.closePrice!!.toBigDecimal(),
    high = this.highPrice!!.toBigDecimal(),
    low = this.lowPrice!!.toBigDecimal(),
    volume = this.volume!!.toLong(),
    transactions = round(this.volume!! / this.averageTradeSize!!).toLong(),
    windowStart = this.startTimestampMillis!!,
    windowEnd = this.endTimestampMillis!! - 1, // we use inclusive start and end, Polygon uses inclusive start and exclusive end
    reset = isReset
)


internal fun Aggregate.toMessage(): Message<Aggregate> =
    MessageBuilder.withPayload(this).setHeader(KafkaHeaders.KEY, this.ticker.toByteArray()).build()