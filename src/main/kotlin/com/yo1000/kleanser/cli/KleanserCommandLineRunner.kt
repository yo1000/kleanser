package com.yo1000.kleanser.cli

import com.yo1000.kleanser.config.MaskIgnoresProperties
import com.yo1000.kleanser.config.ResumeProperties
import com.yo1000.kleanser.repository.GeneralValueMaskRepository
import com.yo1000.kleanser.repository.IrregularValueMaskRepository
import com.yo1000.kleanser.repository.OlderValueMaskRepository
import com.yo1000.kleanser.repository.TableRepository
import com.yo1000.kleanser.service.CleanseService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.stereotype.Component

/**
 *
 * @author yo1000
 */
@Component
class KleanserCommandLineRunner(
        val tableRepository: TableRepository,
        val olderValueMaskRepository: OlderValueMaskRepository,
        val maskedGeneralValueRepository: GeneralValueMaskRepository,
        val irregularValueMaskRepository: IrregularValueMaskRepository,
        val dataSourceProperties: DataSourceProperties,
        val maskIgnoresProperties: MaskIgnoresProperties,
        val resumeProperties: ResumeProperties
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        CleanseService(
                tableRepository,
                listOf(
                        olderValueMaskRepository,
                        maskedGeneralValueRepository,
                        irregularValueMaskRepository
                ),
                maskIgnoresProperties,
                resumeProperties
        ).cleanse(dataSourceProperties.name)
    }
}
