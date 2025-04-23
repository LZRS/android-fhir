package com.google.android.fhir.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.BeforeParam
import org.mitre.synthea.engine.Generator
import org.mitre.synthea.export.Exporter
import org.mitre.synthea.export.Exporter.ExporterRuntimeOptions
import org.mitre.synthea.helpers.Config
import java.util.concurrent.Executors


@RunWith(Parameterized::class)
class DemoBenchmark(private val patientPopulationSize: Int) {

    @get:Rule val benchmarkRule = BenchmarkRule()

    @Test
    fun nothing() {

    }

    private companion object {
//        @Parameterized.Parameters(name = "population: {0}") @JvmStatic fun data() = arrayOf(10_000, 50_000, 100_000, 200_000)
        @Parameterized.Parameters(name = "population: {0}") @JvmStatic fun data() = arrayOf(1, 2, 3)

        @BeforeParam
        @JvmStatic
        fun populateDB(populationSize: Int) {
            val options = Generator.GeneratorOptions().apply {
                enabledModules = listOf("pregnancy")
                population = populationSize
            }

            Config.set("exporter.fhir.included_resources", "Patient")
            Config.set("exporter.fhir.transaction_bundle", "false")
            Config.set("exporter.fhir.use_us_core_ig", "false")
            Config.set("exporter.years_of_history", "1")

            // disable file generation to "output" subdirectory
            Config.set("exporter.fhir.export", "false");
            Config.set("exporter.hospital.fhir.export", "false");
            Config.set("exporter.practitioner.fhir.export", "false");
            val ero = ExporterRuntimeOptions().apply {
                enableQueue(Exporter.SupportedFhirVersion.R4)
            }

            // Create and start generator
            val generator = Generator(options, ero)
            val generatorService = Executors.newFixedThreadPool(1)
            generatorService.submit { generator.run() }


            // Retrieve the generated records
            var recordCount = 0
            while (recordCount < options.population) {
                try {
                    val jsonRecord = ero.nextRecord
                    recordCount++
                    println(jsonRecord.substring(0..<80))
                } catch (ex: InterruptedException) {
                    break
                }
            }

            // Shutdown the generator
            generatorService.shutdownNow()
        }
    }
}