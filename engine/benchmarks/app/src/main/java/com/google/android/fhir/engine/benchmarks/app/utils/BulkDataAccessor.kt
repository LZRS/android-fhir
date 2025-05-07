/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.engine.benchmarks.app.utils

import android.content.res.AssetManager
import java.io.BufferedReader
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

internal class BulkDataAccessor(
  private val assetManager: AssetManager,
) {
  private val openBufferedReaders = mutableListOf<BufferedReader>()

  suspend fun fetchBulkResourcesString(): Sequence<String> =
    withContext(currentCoroutineContext()) {
      val ndjsonFiles =
        assetManager.list(BULK_DATA_DIR)?.filter { it.endsWith(".ndjson") } ?: emptyList()
      return@withContext ndjsonFiles
        .asSequence()
        .map { assetManager.open("$BULK_DATA_DIR/$it") }
        .flatMap {
          val reader = it.bufferedReader()
          openBufferedReaders += reader
          reader.lineSequence()
        }
    }

  suspend fun closeReaders() =
    withContext(currentCoroutineContext()) {
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
