package com.yo1000.kleanser.repository

import com.yo1000.kleanser.model.BroadTypes
import com.yo1000.kleanser.model.Column
import com.yo1000.kleanser.model.Table
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 *
 * @author yo1000
 */
@Repository
class TableRepository(val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun findBySchema(schema: String): List<Table> {
        return jdbcTemplate.query("""
        SELECT
            Tables.TABLE_NAME AS TABLE_NAME
        FROM
            INFORMATION_SCHEMA.TABLES Tables
        WHERE
            Tables.TABLE_SCHEMA = :schemaName
        AND
            Tables.TABLE_TYPE = 'BASE TABLE'
        """, mapOf(
                "schemaName" to schema
        ), { resultSet, _ ->
            resultSet.getString("TABLE_NAME")
        }).map {
            Table(it, jdbcTemplate.query("""
            SELECT
                SubColumns.TABLE_NAME   AS TABLE_NAME,
                SubColumns.COLUMN_NAME  AS COLUMN_NAME,
                SubColumns.DATA_TYPE    AS DATA_TYPE,
                CASE
                    WHEN MAX(SubPrimaries.PRIMARY_KEY) > 0
                    THEN 1
                    ELSE 0
                END AS PRIMARY_KEY,
                CASE
                    WHEN MAX(SubUniques.UNIQUE_KEY) > 0
                    THEN 1
                    ELSE 0
                END AS UNIQUE_KEY,
                CASE
                    WHEN MAX(SubForeign.FOREIGN_KEY) > 0
                    THEN 1
                    ELSE 0
                END AS FOREIGN_KEY
            FROM (
                SELECT
                    TABLE_NAME,
                    COLUMN_NAME,
                    DATA_TYPE
                FROM
                    INFORMATION_SCHEMA.COLUMNS
                WHERE
                    TABLE_SCHEMA = :schemaName
                AND TABLE_NAME = :tableName
                ) SubColumns
            LEFT OUTER JOIN (
                SELECT
                    TABLE_NAME,
                    COLUMN_NAME,
                    SUM(PRIMARY_KEY) AS PRIMARY_KEY
                FROM (
                    SELECT
                        TABLE_NAME,
                        COLUMN_NAME,
                        CASE
                            WHEN
                                INDEX_NAME LIKE 'PRIMARY'
                            THEN 1
                            ELSE 0
                        END AS PRIMARY_KEY
                    FROM
                        INFORMATION_SCHEMA.STATISTICS
                    WHERE
                        TABLE_SCHEMA = :schemaName
                    AND TABLE_NAME = :tableName
                    ) SubPrimaries_Inner1
                GROUP BY
                    TABLE_NAME,
                    COLUMN_NAME
                ) SubPrimaries
                ON  SubColumns.TABLE_NAME  = SubPrimaries.TABLE_NAME
                AND SubColumns.COLUMN_NAME = SubPrimaries.COLUMN_NAME
            LEFT OUTER JOIN (
                SELECT
                    TABLE_NAME,
                    COLUMN_NAME,
                    SUM(UNIQUE_KEY) AS UNIQUE_KEY
                FROM (
                    SELECT
                        TABLE_NAME,
                        COLUMN_NAME,
                        CASE
                            WHEN
                                NON_UNIQUE = 0
                                AND INDEX_NAME NOT LIKE 'PRIMARY'
                            THEN 1
                            ELSE 0
                        END AS UNIQUE_KEY
                    FROM
                        INFORMATION_SCHEMA.STATISTICS
                    WHERE
                        TABLE_SCHEMA = :schemaName
                    AND TABLE_NAME = :tableName
                    ) SubUniques_Inner1
                GROUP BY
                    TABLE_NAME,
                    COLUMN_NAME
                ) SubUniques
                ON  SubColumns.TABLE_NAME  = SubUniques.TABLE_NAME
                AND SubColumns.COLUMN_NAME = SubUniques.COLUMN_NAME
            LEFT OUTER JOIN (
                SELECT
                    SubForeign_Inner1.TABLE_NAME,
                    SubForeign_Inner1.COLUMN_NAME,
                    CASE
                        WHEN SubForeign_Inner2.FOREIGN_KEY IS NOT NULL THEN 1
                        WHEN SubForeign_Inner3.FOREIGN_KEY IS NOT NULL THEN 1
                        ELSE 0
                    END AS FOREIGN_KEY
                FROM (
                    SELECT
                        TABLE_NAME,
                        COLUMN_NAME
                    FROM
                        INFORMATION_SCHEMA.COLUMNS
                    WHERE
                        TABLE_SCHEMA = :schemaName
                    AND TABLE_NAME = :tableName
                    ) SubForeign_Inner1
                LEFT OUTER JOIN (
                    SELECT
                        TABLE_NAME,
                        COLUMN_NAME,
                        1 AS FOREIGN_KEY
                    FROM
                        INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                    WHERE
                        TABLE_SCHEMA = :schemaName
                    AND TABLE_NAME = :tableName
                    GROUP BY
                        TABLE_NAME,
                        COLUMN_NAME
                    ) SubForeign_Inner2
                    ON  SubForeign_Inner1.TABLE_NAME = SubForeign_Inner2.TABLE_NAME
                    AND SubForeign_Inner1.COLUMN_NAME = SubForeign_Inner2.COLUMN_NAME
                LEFT OUTER JOIN (
                    SELECT
                        REFERENCED_TABLE_NAME AS TABLE_NAME,
                        REFERENCED_COLUMN_NAME AS COLUMN_NAME,
                        1 AS FOREIGN_KEY
                    FROM
                        INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                    WHERE
                        TABLE_SCHEMA = :schemaName
                    AND REFERENCED_TABLE_NAME = :tableName
                    GROUP BY
                        REFERENCED_TABLE_NAME,
                        REFERENCED_COLUMN_NAME
                    ) SubForeign_Inner3
                    ON  SubForeign_Inner1.TABLE_NAME = SubForeign_Inner3.TABLE_NAME
                    AND SubForeign_Inner1.COLUMN_NAME = SubForeign_Inner3.COLUMN_NAME
                GROUP BY
                    SubForeign_Inner1.TABLE_NAME,
                    SubForeign_Inner1.COLUMN_NAME,
                    SubForeign_Inner2.FOREIGN_KEY,
                    SubForeign_Inner3.FOREIGN_KEY
                ) SubForeign
                ON  SubColumns.TABLE_NAME  = SubForeign.TABLE_NAME
                AND SubColumns.COLUMN_NAME = SubForeign.COLUMN_NAME
            GROUP BY
                SubColumns.TABLE_NAME,
                SubColumns.COLUMN_NAME,
                SubColumns.DATA_TYPE
            """, mapOf(
                    "schemaName" to schema,
                    "tableName" to it
            ), { resultSet, _ ->
                val dataType = resultSet.getString("DATA_TYPE")

                Column(
                        resultSet.getString("COLUMN_NAME"),
                        dataType,
                        BroadTypes.parseBroadTypeFromMysqlDataType(dataType),
                        resultSet.getInt("PRIMARY_KEY") == 1,
                        resultSet.getInt("UNIQUE_KEY") == 1,
                        resultSet.getInt("FOREIGN_KEY") == 1
                )
            }))
        }
    }
}
