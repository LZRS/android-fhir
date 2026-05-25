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

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * Contract for persisting sync state across sessions.
 *
 * Keeping this as an interface (rather than a concrete class) allows [FhirSyncCore] and
 * [FhirSynchronizer] to depend on a platform-neutral contract. The Android-specific implementation
 * backed by Jetpack DataStore is [AndroidFhirDataStore].
 *
 * When the codebase migrates to KMP, this interface (and all types it references) moves to
 * `commonMain`, while [AndroidFhirDataStore] stays in `androidMain`.
 */
@PublishedApi
internal interface FhirDataStore {

  fun observeTerminalSyncJobStatus(key: String): Flow<SyncJobStatus?>

  suspend fun writeTerminalSyncJobStatus(key: String, syncJobStatus: SyncJobStatus)

  fun readLastSyncTimestamp(): OffsetDateTime?

  fun writeLastSyncTimestamp(datetime: OffsetDateTime)

  suspend fun storeUniqueWorkName(key: String, value: String)

  suspend fun removeUniqueWorkName(key: String)

  suspend fun fetchUniqueWorkName(key: String): String?
}

/** Android implementation of [FhirDataStore] backed by Jetpack DataStore preferences. */
internal class AndroidFhirDataStore(context: Context) : FhirDataStore {
  private val Context.dataStore by preferencesDataStore(name = FHIR_PREFERENCES_NAME)
  private val dataStore = context.dataStore
  private val serializer = SyncJobStatus.SyncJobStatusSerializer()
  private val syncJobStatusFlowMap = mutableMapOf<String, Flow<SyncJobStatus?>>()
  private val lastSyncTimestampKey by lazy { stringPreferencesKey(LAST_SYNC_TIMESTAMP) }

  /**
   * Observes the sync job terminal state for a given key and provides it as a Flow.
   *
   * @param key The key associated with the sync job.
   * @return A Flow of [SyncJobStatus] representing the terminal state of the sync job, or null if
   *   the state is not allowed.
   */
  override fun observeTerminalSyncJobStatus(key: String): Flow<SyncJobStatus?> =
    syncJobStatusFlowMap.getOrPut(key) {
      dataStore.data
        .catch { exception ->
          if (exception is IOException) {
            Timber.e(exception)
            emit(emptyPreferences())
          } else {
            Timber.e(exception)
            throw exception
          }
        }
        .map { preferences -> serializer.deserialize(preferences[stringPreferencesKey(key)]) }
    }

  /**
   * Edits the DataStore to store synchronization job status. It creates a data object containing
   * the state type and serialized state of the synchronization job status. The edited preferences
   * are updated with the serialized data.
   *
   * @param syncJobStatus The synchronization job status to be stored.
   * @param key The key associated with the data to edit.
   */
  override suspend fun writeTerminalSyncJobStatus(
    key: String,
    syncJobStatus: SyncJobStatus,
  ) {
    when (syncJobStatus) {
      is SyncJobStatus.Succeeded,
      is SyncJobStatus.Failed, -> {
        writeSyncJobStatus(key, syncJobStatus)
      }
      else -> error("Do not write non-terminal state")
    }
  }

  private suspend fun writeSyncJobStatus(key: String, syncJobStatus: SyncJobStatus) {
    dataStore.edit { preferences ->
      preferences[stringPreferencesKey(key)] = serializer.serialize(syncJobStatus)
    }
  }

  override fun readLastSyncTimestamp(): OffsetDateTime? {
    val millis = runBlocking { dataStore.data.first()[lastSyncTimestampKey] } ?: return null
    return OffsetDateTime.parse(millis)
  }

  override fun writeLastSyncTimestamp(datetime: OffsetDateTime) {
    runBlocking { dataStore.edit { pref -> pref[lastSyncTimestampKey] = datetime.toString() } }
  }

  private val mutexMap = ConcurrentHashMap<String, Mutex>()

  private fun getOrCreateMutex(key: String): Mutex {
    return mutexMap.computeIfAbsent(key) { Mutex() }
  }

  /** Stores the given unique-work-name in DataStore. */
  override suspend fun storeUniqueWorkName(key: String, value: String) {
    getOrCreateMutex(key).withLock {
      dataStore.edit { preferences -> preferences[stringPreferencesKey("$key-key")] = value }
    }
  }

  override suspend fun removeUniqueWorkName(key: String) {
    getOrCreateMutex(key).withLock {
      dataStore.edit { preferences ->
        val value = preferences.remove(stringPreferencesKey("$key-key"))
        Timber.d("Removed value: $value")
      }
    }
  }

  /** Fetches the stored unique-work-name from DataStore. */
  override suspend fun fetchUniqueWorkName(key: String): String? {
    return getOrCreateMutex(key).withLock {
      val preferences = dataStore.data.first()
      preferences[stringPreferencesKey("$key-key")]
    }
  }

  private companion object {
    private const val FHIR_PREFERENCES_NAME = "FHIR_ENGINE_PREF_DATASTORE"
    private const val LAST_SYNC_TIMESTAMP = "LAST_SYNC_TIMESTAMP"
  }
}
