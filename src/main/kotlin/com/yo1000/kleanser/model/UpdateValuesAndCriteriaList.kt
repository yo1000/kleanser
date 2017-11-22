package com.yo1000.kleanser.model

/**
 *
 * @author yo1000
 */
data class UpdateValuesAndCriteriaList(
        private val updateValuesAndCriteria: List<UpdateValuesAndCriteria<Boolean>>
): ArrayList<UpdateValuesAndCriteria<Boolean>>(updateValuesAndCriteria) {
    private val columnsThatRequireMask =  if (this.isEmpty()) emptyMap<String, Boolean>() else this.map {
            it.updateValues.map { it.key to it.value.attribute }
        }.reduce { accumulator, accumulatee ->
            accumulator.map { acc ->
                if (acc.second) acc else accumulatee.find { it.first == acc.first } ?: acc
            }
        }.associate { it }

    fun requiresMask(columnName: String): Boolean {
        return columnsThatRequireMask.getOrDefault(columnName, false)
    }
}
