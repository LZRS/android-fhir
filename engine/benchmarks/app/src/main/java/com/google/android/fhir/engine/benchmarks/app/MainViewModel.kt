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

package com.google.android.fhir.engine.benchmarks.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.tracing.trace
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.engine.benchmarks.app.utils.BulkDataAccessor
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import kotlin.time.measureTimedValue

@OptIn(ExperimentalCoroutinesApi::class)
private val mainViewModelDispatcher = Dispatchers.Default.limitedParallelism(1)

class MainViewModel(application: Application, private val fhirEngine: FhirEngine) :
  AndroidViewModel(application) {

  private val assetManager by lazy { getApplication<Application>().assets }
  private val fhirR4JsonParser by lazy { FhirContext.forCached(FhirVersionEnum.R4).newJsonParser() }

  private val createMutableStateFlow = MutableStateFlow<List<BenchmarkDuration>>(listOf())
  val createStateFlow = createMutableStateFlow.asStateFlow()

  private val getMutableStateFlow = MutableStateFlow<List<BenchmarkDuration>>(listOf())
  val getStateFlow = getMutableStateFlow.asStateFlow()

  private val updateMutableStateFlow = MutableStateFlow<List<BenchmarkDuration>>(listOf())
  val updateStateFlow = updateMutableStateFlow.asStateFlow()

  private val deleteMutableStateFlow = MutableStateFlow<List<BenchmarkDuration>>(listOf())
  val deleteStateFlow = deleteMutableStateFlow.asStateFlow()

  /**
   * todo: on top of trace api, also use measuretime to get time that could be displayed in UI trace
   * will be for benchmarking, and measuretime for user display (approximate time)
   */
  private fun traceCreateResources(resources: List<Resource>) =
      measureTimedValue {
        trace(TRACE_CREATE_SECTION_NAME) {
          runBlocking { fhirEngine.create(*resources.toTypedArray()) }
        }
    }

  private fun traceUpdateResources(resources: List<Resource>) =
      measureTime {
        trace(TRACE_UPDATE_SECTION_NAME) {
          runBlocking { fhirEngine.update(*resources.toTypedArray()) }
        }
    }

  private fun traceGetResource(resourceType: ResourceType, resourceId: String) =
      measureTimedValue {
        trace(TRACE_GET_SECTION_NAME) { runBlocking { fhirEngine.get(resourceType, resourceId) } }
    }

  private fun traceDeleteResources(resourceType: ResourceType, resourceId: String) =
      measureTime {
        trace(TRACE_DELETE_SECTION_NAME) {
          runBlocking { fhirEngine.delete(resourceType, resourceId) }
        }
    }

  fun traceDataAccess() {
    viewModelScope.launch(mainViewModelDispatcher) {
      val dataAccessor = BulkDataAccessor(assetManager)
      // Create
      fhirEngine.clearDatabase()
      val savedResourceTypeIdPairs: Iterable<List<Pair<ResourceType, String>>>
      try {
        savedResourceTypeIdPairs = dataAccessor.fetchBulkResourcesString()
          .chunked(PROCESS_RESOURCES_CHUNK_SIZE)
          .map {
            it.map { resourceStr -> fhirR4JsonParser.parseResource(resourceStr) as Resource }
          }
          .map { resources ->
            val (logicalIds, duration) = traceCreateResources(resources)
          createMutableStateFlow.update {
            it + BenchmarkDuration(resources.size, duration)
          }
            resources.zip(logicalIds){ r, l -> Pair(r.resourceType, l) }
        }.toList()
      } finally {
        dataAccessor.closeReaders()
      }

      // Get
      val dbResources = savedResourceTypeIdPairs.mapIndexed { index, list ->
        val (resourceType, logicalId) = list.shuffled().random()
        val (resource, duration) =  traceGetResource(resourceType, logicalId)
        getMutableStateFlow.update {
          it + BenchmarkDuration(index, duration)
        }
        resource
      }

      // Update
      val updateDbResources = dbResources.shuffled().mapIndexed { index, resource ->
        val duration = traceUpdateResources(listOf(resource))
        updateMutableStateFlow.update { it + BenchmarkDuration(index, duration) }
        resource
      }

      // Delete
      updateDbResources.shuffled().forEachIndexed { index, resource ->
        val logicalId = resource.idElement?.idPart.orEmpty()
        val duration = runBlocking { traceDeleteResources(resource.resourceType, logicalId) }
        deleteMutableStateFlow.update { it + BenchmarkDuration(index, duration) }
      }
    }
  }

  companion object {
    private const val PROCESS_RESOURCES_CHUNK_SIZE = 50

    const val TRACE_CREATE_SECTION_NAME = "Create API"
    const val TRACE_UPDATE_SECTION_NAME = "Update API"
    const val TRACE_GET_SECTION_NAME = "Get API"
    const val TRACE_DELETE_SECTION_NAME = "Delete API"
  }
}

class MainViewModelFactory(
  private val application: Application,
  private val fhirEngine: FhirEngine,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      return MainViewModel(application, fhirEngine) as T
    }
    return super.create(modelClass)
  }
}

typealias BenchmarkDuration = Pair<Int, Duration>
