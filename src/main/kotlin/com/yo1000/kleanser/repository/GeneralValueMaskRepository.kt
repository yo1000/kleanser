package com.yo1000.kleanser.repository

import com.yo1000.kleanser.model.*
import com.yo1000.kleanser.util.MaskUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 *
 * @author yo1000
 */
@Repository
class GeneralValueMaskRepository(
        override val jdbcTemplate: NamedParameterJdbcTemplate
): ValueMaskRepository {
    override fun mask(table: Table) {
        if (!table.containsStringColumns()) {
            return
        }

        val valuesAndCriteriaList = UpdateValuesAndCriteriaList(jdbcTemplate.query("""
        SELECT
            ${table.listPrimaryKeyColumnNames().joinToString(separator = ",", postfix = ",")}
            ${table.listStringColumnNames().joinToString(separator = ",")}
        FROM
            ${table.name}
        """, { resultSet, _ ->
            UpdateValuesAndCriteria(
                    table.listStringColumnNames().associate {
                        val s = resultSet.getString(it)
                        it to UpdateValue(MaskUtils.maskStringValue(s), MaskUtils.requiresMaskStringValue(s))
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
                ${updateValuesAndCriteria.criteria.entries.map {
                    "${it.key} = :${updateValuesAndCriteria.prefixParamCriterion}${it.key}"
                }.joinToString(separator = " AND ")}
            """, updateValuesAndCriteria.toParameters())
        }
    }
}
