package com.yo1000.kleanser.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.yo1000.kleanser.config.MaskIgnoresProperties
import com.yo1000.kleanser.config.ResumeProperties
import com.yo1000.kleanser.model.Table
import com.yo1000.kleanser.repository.ValueMaskRepository
import com.yo1000.kleanser.repository.TableRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 *
 * @author yo1000
 */
class CleanseService(
        private val tableRepository: TableRepository,
        private val maskRepositories: List<ValueMaskRepository>,
        private val ignoreProps: MaskIgnoresProperties,
        private val resumeProps: ResumeProperties
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(CleanseService::class.java)
    }

    private fun scanTables(filePath: Path, schemaName: String): List<Table> {
        if (Files.exists(filePath)) {
            return scanTablesByFile(filePath.toFile())
        } else {
            return scanTablesByDatabase(filePath.toFile(), schemaName)
        }
    }

    private fun scanTablesByDatabase(file: File, schemaName: String): List<Table> {
        LOGGER.info("Scanning tables...")
        val tables = tableRepository.findBySchema(schemaName)
        LOGGER.info("Scanning tables is done.")

        val ignoreTableColumns = mutableMapOf<String, List<String>>()
        ignoreProps.tableColumns.map {
            it.split(".")
        }.filter {
            it.size == 2
        }.forEach {
            ignoreTableColumns.put(it[0], (ignoreTableColumns[it[0]] ?: mutableListOf()) + it[1])
        }

        val requiresTables = tables.map {
            val columnNames = ignoreTableColumns[it.name].orEmpty()
            it.copy(columns = it.columns.filter {
                !columnNames.contains(it.name)
            })
        }.sortedBy { it.name }

        FileWriter(file).use {
            ObjectMapper().writeValue(it, requiresTables)
        }

        return requiresTables
    }

    private fun scanTablesByFile(file: File): List<Table> {
        FileReader(file).use {
            return ObjectMapper().readValue(it, object : TypeReference<List<Table>>() {})
        }
    }

    fun cleanse(schemaName: String) {
        val doneTablesFile = Paths.get(resumeProps.progressFilePath).toFile()
        val doneTableNames = if (doneTablesFile.exists()) {
            doneTablesFile.bufferedReader().use {
                it.readLines()
            }
        } else {
            emptyList()
        }

        val allTables = scanTables(Paths.get(resumeProps.tablesFilePath), schemaName)
        val tables = allTables.filter {
            !doneTableNames.contains(it.name)
        }

        var doneCount = allTables.size - tables.size
        tables.parallelStream().forEach { table ->
            LOGGER.debug("Masking table({}/{}): {}", doneCount + 1, allTables.size, table)
            maskRepositories.forEach { maskRepository ->
                maskRepository.mask(table)
                LOGGER.info("Starting mask: {}", maskRepository::class.simpleName)
            }
            FileWriter(resumeProps.progressFilePath, true).use {
                it.appendln(table.name)
            }
            doneCount++
        }
    }
}
