package com.google.android.fhir.engine.benchmarks.app.utils

import android.content.res.AssetManager
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.Resource
import java.io.BufferedReader

internal class BulkDataAccessor(
    private val assetManager: AssetManager,
    private val fhirR4JsonParser: IParser
) {
    private val openBufferedReaders = mutableListOf<BufferedReader>()


    fun fetchBulkResources(): Sequence<Resource> {
        val ndjsonFiles =
            assetManager.list(BULK_DATA_DIR)?.filter { it.endsWith(".ndjson") } ?: emptyList()
        return ndjsonFiles.asSequence()
            .map { assetManager.open("$BULK_DATA_DIR/$it") }
            .flatMap {
                val reader = it.bufferedReader()
                openBufferedReaders += reader
                reader.lineSequence()
            }
            .map { fhirR4JsonParser.parseResource(it) as Resource }
    }

    fun closeReaders() {
        openBufferedReaders.forEach {
            try {
                it.close()
            } finally {
                openBufferedReaders -= it
            }
        }
    }

    companion object {
        private const val BULK_DATA_DIR = "bulk_data"
    }
}