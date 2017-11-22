package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
data class UpdateValue<out A>(
        val value: Any?,
        val attribute: A
)
