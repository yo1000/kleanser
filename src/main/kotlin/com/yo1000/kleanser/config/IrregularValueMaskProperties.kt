package com.yo1000.kleanser.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 *
 * @author yo1000
 */
@Configuration
@ConfigurationProperties("kleanser.irregular-value-mask")
data class IrregularValueMaskProperties(
        var tableColumns: List<String> = emptyList()
)
