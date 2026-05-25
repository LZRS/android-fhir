/*
 * Copyright 2023-2025 Google LLC
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

package com.google.android.fhir.sync

import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.download.DownloaderImpl
import com.google.android.fhir.sync.upload.UploadStrategy
import com.google.android.fhir.sync.upload.Uploader
import com.google.android.fhir.sync.upload.patch.PatchGeneratorFactory
import com.google.android.fhir.sync.upload.request.UploadRequestGeneratorFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Platform-agnostic sync execution kernel.
 *
 * Builds the [FhirSynchronizer], drives the sync state machine, persists terminal status to
 * [FhirDataStore], and returns the final [SyncJobStatus]. All logic here is pure Kotlin coroutines
 * with no dependency on Android WorkManager or any other platform API.
 *
 * [FhirSyncWorker] is the Android WorkManager wrapper that delegates to this class. When the
 * codebase migrates to KMP, this class moves to `commonMain` while [FhirSyncWorker] stays in
 * `androidMain`.
 */
internal class FhirSyncCore(
  private val fhirEngine: FhirEngine,
  private val dataSource: DataSource,
  private val downloadWorkManager: DownloadWorkManager,
  private val conflictResolver: ConflictResolver,
  private val uploadStrategy: UploadStrategy,
  private val fhirDataStore: FhirDataStore,
) {

  /**
   * Runs a full sync cycle (download then upload) and returns the terminal [SyncJobStatus].
   *
   * @param workerName Unique name used to persist the terminal status in [FhirDataStore]. Null when
   *   the sync was not scheduled via [Sync] (e.g. in tests or one-off invocations).
   * @param onProgress Called for every non-terminal [SyncJobStatus] emission so the caller can
   *   forward progress via its own signalling mechanism (e.g. WorkManager's `setProgress`).
   */
  suspend fun execute(
    workerName: String?,
    onProgress: suspend (SyncJobStatus) -> Unit,
  ): SyncJobStatus {
    val synchronizer =
      FhirSynchronizer(
        fhirEngine,
        UploadConfiguration(
          uploader =
            Uploader(
              dataSource = dataSource,
              patchGenerator = PatchGeneratorFactory.byMode(uploadStrategy.patchGeneratorMode),
              requestGenerator =
                UploadRequestGeneratorFactory.byMode(uploadStrategy.requestGeneratorMode),
            ),
          uploadStrategy = uploadStrategy,
        ),
        DownloadConfiguration(
          DownloaderImpl(dataSource, downloadWorkManager),
          conflictResolver,
        ),
        fhirDataStore,
      )

    val job =
      CoroutineScope(Dispatchers.IO).launch {
        synchronizer.syncState.collect { syncJobStatus ->
          when (syncJobStatus) {
            is SyncJobStatus.Succeeded,
            is SyncJobStatus.Failed -> {
              if (workerName != null) {
                fhirDataStore.writeTerminalSyncJobStatus(workerName, syncJobStatus)
              }
              cancel()
            }
            else -> onProgress(syncJobStatus)
          }
        }
      }

    val result = synchronizer.synchronize()
    kotlin.runCatching { job.join() }.onFailure(Timber::w)
    return result
  }
}
