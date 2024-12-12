package com.tradeEngine.polygonTickersConnector.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PolygonTickersConnectorApplication

fun main(args: Array<String>) {
    runApplication<PolygonTickersConnectorApplication>(*args)
}
