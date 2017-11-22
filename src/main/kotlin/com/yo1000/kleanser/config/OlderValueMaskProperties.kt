package com.yo1000.kleanser.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 *
 * @author yo1000
 */
@Configuration
@ConfigurationProperties("kleanser.older-value-mask")
data class OlderValueMaskProperties(
        var createdAtColumnName: String = "",
        var updatedAtColumnName: String = "",
        var boundaryDate: Date = Date(0L)
)
