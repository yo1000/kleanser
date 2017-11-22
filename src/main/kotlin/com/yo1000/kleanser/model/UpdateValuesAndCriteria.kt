package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
data class UpdateValuesAndCriteria<out A>(
        val updateValues: Map<String, UpdateValue<A>>,
        val criteria: Map<String, Any>,
        val prefixParamValue: String = UpdateValuesAndCriteria.DEFAULT_PREFIX_PARAM_VALUE,
        val prefixParamCriterion: String = UpdateValuesAndCriteria.DEFAULT_PREFIX_PARAM_CRITERION
) {
    companion object {
        const val DEFAULT_PREFIX_PARAM_VALUE = "V_"
        const val DEFAULT_PREFIX_PARAM_CRITERION = "C_"
    }

    fun toParameters(): Map<String, Any?> {
        val params = mutableMapOf<String, Any?>()
        updateValues.entries.forEach {
            params.put("$prefixParamValue${it.key}", it.value.value)
        }
        criteria.entries.forEach {
            params.put("$prefixParamCriterion${it.key}", it.value)
        }
        return params
    }
}
