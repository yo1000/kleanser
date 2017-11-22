package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
data class Column(
        val name: String,
        val type: String,
        val broadType: BroadTypes,
        val partOfPrimaryKey: Boolean,
        val partOfUniqueKey: Boolean,
        val partOfForeignKey: Boolean
)
