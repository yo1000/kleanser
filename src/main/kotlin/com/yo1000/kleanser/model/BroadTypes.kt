package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
enum class BroadTypes(val mysqlDataTypes: List<String>) {
    STRING(listOf(
            "CHAR", "VARCHAR", "BINARY", "VARBINARY", "BLOB", "TEXT", "ENUM", "SET"
    )),
    INTEGER(listOf(
            "INTEGER", "INT", "SMALLINT", "TINYINT", "MEDIUMINT", "BIGINT", "BIT"
    )),
    FLOAT(listOf(
            "FLOAT", "DOUBLE", "REAL"
    )),
    DECIMAL(listOf(
            "DECIMAL", "NUMERIC"
    )),
    DATE(listOf(
            "DATE", "TIME", "DATETIME", "TIMESTAMP", "YEAR"
    )),
    NULL(listOf()),
    OTHER(listOf());

    companion object {
        fun parseBroadTypeFromMysqlDataType(dataType: String): BroadTypes {
            return BroadTypes.values().find {
                it.mysqlDataTypes.any {
                    it.toLowerCase() == dataType.toLowerCase()
                }
            } ?: OTHER
        }
    }
}