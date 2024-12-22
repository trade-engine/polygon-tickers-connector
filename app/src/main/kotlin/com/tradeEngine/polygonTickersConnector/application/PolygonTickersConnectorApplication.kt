package com.tradeEngine.polygonTickersConnector.application

import com.tradeEngine.polygonTickersConnector.application.configuration.ApplicationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfiguration::class)
@EnableScheduling
internal open class PolygonTickersConnectorApplication

internal fun main(args: Array<String>) {
    runApplication<PolygonTickersConnectorApplication>(*args)
}
