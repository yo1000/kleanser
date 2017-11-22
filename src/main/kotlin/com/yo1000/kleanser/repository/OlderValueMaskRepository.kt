package com.yo1000.kleanser.repository

import com.yo1000.kleanser.config.OlderValueMaskProperties
import com.yo1000.kleanser.model.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @author yo1000
 */
@Repository
class OlderValueMaskRepository(
        override val jdbcTemplate: NamedParameterJdbcTemplate,
        val props: OlderValueMaskProperties
) : ValueMaskRepository {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(OlderValueMaskRepository::class.java)
    }

    private fun deleteByCreatedAt(tableName: String, dateTime: Date) {
        val dateTimeParam = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateTime)
        jdbcTemplate.jdbcOperations.batchUpdate(
                "SET FOREIGN_KEY_CHECKS=0",
                """
                DELETE FROM $tableName
                WHERE ${props.createdAtColumnName} < '$dateTimeParam'
                """)
    }

    private fun deleteByUpdatedAt(tableName: String, dateTime: Date) {
        val dateTimeParam = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateTime)
        jdbcTemplate.jdbcOperations.batchUpdate(
                "SET FOREIGN_KEY_CHECKS=0",
                """
                DELETE FROM $tableName
                WHERE ${props.updatedAtColumnName} < '$dateTimeParam'
                """)
    }

    override fun mask(table: Table) {
        val dateTimeText = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(props.boundaryDate)

        if (table.columns.filter {
            it.name == props.createdAtColumnName
        }.count() > 0) {
            LOGGER.debug("Delete {} by created at before {}", table.name, dateTimeText)
            deleteByCreatedAt(table.name, props.boundaryDate)
            return
        }

        if (table.columns.filter {
            it.name == props.updatedAtColumnName
        }.count() > 0) {
            LOGGER.debug("Delete {} by updated at before {}", table.name, dateTimeText)
            deleteByUpdatedAt(table.name, props.boundaryDate)
            return
        }
    }
}
