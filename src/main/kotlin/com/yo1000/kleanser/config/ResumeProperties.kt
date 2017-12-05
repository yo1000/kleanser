package com.yo1000.kleanser.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 *
 * @author yo1000
 */
@Configuration
@ConfigurationProperties("kleanser.resume")
data class ResumeProperties(
    var tablesFilePath: String = "tables.json",
    var progressFilePath: String = "tables.progress"
)
