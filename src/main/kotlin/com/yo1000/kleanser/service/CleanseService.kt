package com.yo1000.kleanser.service

import com.yo1000.kleanser.config.MaskIgnoresProperties
import com.yo1000.kleanser.repository.ValueMaskRepository
import com.yo1000.kleanser.repository.TableRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

/**
 *
 * @author yo1000
 */
class CleanseService(
        private val tableRepository: TableRepository,
        private val maskRepositories: List<ValueMaskRepository>,
        private val props: MaskIgnoresProperties
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(CleanseService::class.java)
    }

    @Transactional
    fun cleanse(schemaName: String) {
        LOGGER.info("Scanning tables...")
        val tables = tableRepository.findBySchema(schemaName)
        LOGGER.info("Scanning tables is done.")

        val ignoreTableColumns = mutableMapOf<String, List<String>>()
        props.tableColumns.map {
            it.split(".")
        }.filter {
            it.size == 2
        }.forEach {
            ignoreTableColumns.put(it[0], (ignoreTableColumns[it[0]] ?: mutableListOf()) + it[1])
        }

        val requiredTables = tables.map {
            val columnNames = ignoreTableColumns[it.name].orEmpty()
            it.copy(columns = it.columns.filter {
                !columnNames.contains(it.name)
            })
        }

        maskRepositories.forEach { maskRepository ->
            LOGGER.info("Starting mask: {}", maskRepository::class.simpleName)
            requiredTables.forEachIndexed { index, table ->
                LOGGER.debug("Masking table({}/{}): {}", index + 1, requiredTables.size, table)
                maskRepository.mask(table)
            }
        }
    }
}
