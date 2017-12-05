package com.yo1000.kleanser.repository

import com.yo1000.kleanser.config.IrregularValueMaskProperties
import com.yo1000.kleanser.model.*
import com.yo1000.kleanser.util.MaskUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 *
 * @author yo1000
 */
@Repository
class IrregularValueMaskRepository(
        override val jdbcTemplate: NamedParameterJdbcTemplate,
        val props: IrregularValueMaskProperties
) : ValueMaskRepository {
    override fun mask(table: Table) {
        val tableColumns = mutableMapOf<String, List<String>>()
        props.tableColumns.map {
            it.split(".")
        }.filter {
            it.size == 2
        }.forEach {
            tableColumns.put(it[0], (tableColumns[it[0]] ?: mutableListOf()) + it[1])
        }

        val columns = table.columns.filter { (columnName) ->
            tableColumns.any { (propsTableName, propsColumnNames) ->
                propsTableName == table.name && propsColumnNames.any { it == columnName }
            }
        }

        if (columns.isEmpty()) {
            return
        }

        val valuesAndCriteriaList = UpdateValuesAndCriteriaList(jdbcTemplate.query("""
        SELECT
            ${table.listPrimaryKeyColumnNames().joinToString(separator = ",", postfix = ",")}
            ${columns.joinToString(separator = ",") { it.name }}
        FROM
            ${table.name}
        """, { resultSet, _ ->
            UpdateValuesAndCriteria(
                    columns.associate {
                        it.name to when (it.broadType) {
                            BroadTypes.STRING -> {
                                val value = resultSet.getString(it.name)
                                UpdateValue(MaskUtils.maskStringValue(value), true)
                            }
                            BroadTypes.INTEGER -> {
                                val value = resultSet.getLong(it.name)
                                UpdateValue(MaskUtils.maskIntegerValue(value), true)
                            }
                            BroadTypes.FLOAT -> {
                                val value = resultSet.getDouble(it.name)
                                UpdateValue(MaskUtils.maskFloatValue(value), true)
                            }
                            BroadTypes.DECIMAL -> {
                                val value = resultSet.getBigDecimal(it.name)
                                UpdateValue(MaskUtils.maskDecimalValue(value), true)
                            }
                            else -> {
                                UpdateValue(resultSet.getObject(it.name), false)
                            }
                        }
                    },
                    table.listPrimaryKeyColumnNames().associate {
                        it to resultSet.getObject(it)
                    })
        }))

        valuesAndCriteriaList.filter {
            it.updateValues.entries.any { valuesAndCriteriaList.requiresMask(it.key) }
        }.parallelStream().forEach { updateValuesAndCriteria ->
            jdbcTemplate.update("""
            UPDATE
                ${table.name}
            SET
                ${updateValuesAndCriteria.updateValues.entries.filter {
                valuesAndCriteriaList.requiresMask(it.key)
            }.map {
                "${it.key} = :${updateValuesAndCriteria.prefixParamValue}${it.key}"
            }.joinToString(separator = ",")}
            WHERE
                ${updateValuesAndCriteria.criteria.entries.joinToString(separator = " AND ") {
                    "${it.key} = :${updateValuesAndCriteria.prefixParamCriterion}${it.key}"
                }}
            """, updateValuesAndCriteria.toParameters())
        }
    }
}
