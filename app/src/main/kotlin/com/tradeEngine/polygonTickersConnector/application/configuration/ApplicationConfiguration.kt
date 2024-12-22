package com.tradeEngine.polygonTickersConnector.application.configuration

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "application")
@Validated
internal data class ApplicationConfiguration(
    @field:Valid val api: Api
) {

    data class Api(@field:NotBlank val key: String)
}
