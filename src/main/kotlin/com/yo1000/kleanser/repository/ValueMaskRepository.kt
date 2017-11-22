package com.yo1000.kleanser.repository

import com.yo1000.kleanser.model.Table
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 *
 * @author yo1000
 */
interface ValueMaskRepository {
    val jdbcTemplate: NamedParameterJdbcTemplate
    fun mask(table: Table)
}
