package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
data class Table(
        val name: String,
        val columns: List<Column>
) {
    fun containsStringColumns(): Boolean {
        return columns.any {
            it.broadType == BroadTypes.STRING && !it.partOfPrimaryKey && !it.partOfUniqueKey && !it.partOfForeignKey
        } && columns.any {
            it.partOfPrimaryKey
        }
    }

    fun listPrimaryKeyColumnNames(): List<String> {
        return columns.filter {
            it.partOfPrimaryKey
        }.map {
            it.name
        }
    }

    fun listStringColumnNames(): List<String> {
        return columns.filter {
            it.broadType == BroadTypes.STRING && !it.partOfPrimaryKey && !it.partOfUniqueKey && !it.partOfForeignKey
        }.map {
            it.name
        }
    }
}