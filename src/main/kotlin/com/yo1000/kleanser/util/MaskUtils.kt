package com.yo1000.kleanser.util

import java.math.BigDecimal

/**
 *
 * @author yo1000
 */
class MaskUtils {
    companion object {
        fun requiresMaskStringValue(value: String?): Boolean {
            if (value == null) return false
            return value.any { it !in '0'..'9' && it !in 'A'..'Z' && it !in 'a'..'z' }
        }

        fun maskStringValue(value: String?): String? {
            if (value == null) return null
            return value.mapIndexed { index, c ->
                if (index % 5 == 0) c else '*'
            }.joinToString(separator = "")
        }

        fun maskIntegerValue(value: Long?): Long? {
            if (value == null) return null
            return value.toString().mapIndexed { i, c ->
                    if (i % 2 == 0) '0' else c
            }.joinToString(separator = "").toLong()
        }

        fun maskFloatValue(value: Double?): Double? {
            if (value == null) return null
            return value.toString().split(".").map {
                it.mapIndexed { i, c ->
                    if (i % 2 == 0) '0' else c
                }.joinToString(separator = "")
            }.joinToString(separator = ".").toDouble()
        }

        fun maskDecimalValue(value: BigDecimal?): BigDecimal? {
            if (value == null) return null
            return BigDecimal(value.toString().split(".").map {
                it.mapIndexed { i, c ->
                    if (i % 2 == 0) '0' else c
                }.joinToString(separator = "")
            }.joinToString(separator = "."))
        }
    }
}