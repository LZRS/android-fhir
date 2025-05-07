package com.google.android.fhir.engine.benchmarks.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.tracing.trace
import androidx.tracing.traceAsync
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.engine.benchmarks.app.utils.BulkDataAccessor
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
import java.io.BufferedReader
import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
private val tracingDispatcher = Dispatchers.Default.limitedParallelism(1)

class MainViewModel(application: Application, private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {

    private val assetManager by lazy { getApplication<Application>().assets }
    private val fhirR4JsonParser by lazy {
        FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    }

    private val createMutableStateFlow = MutableStateFlow<CreateUiState>(CreateUiState.Loading)
    val createStateFlow = createMutableStateFlow.asStateFlow()

    /**
     * todo: on top of trace api, also use measuretime to get time that could be displayed in UI
     *  trace will be for benchmarking, and measuretime for user display (approximate time)
     */
    suspend fun traceCreateResources(resources: List<Resource>) =
        withContext(tracingDispatcher) {
            measureTime {
                trace(TRACE_CREATE_SECTION_NAME) {
                    runBlocking { fhirEngine.create(*resources.toTypedArray()) }
                }
            }
        }

    suspend fun traceUpdateResources(resources: List<Resource>) =
        withContext(tracingDispatcher) {
            measureTime {
                trace(TRACE_UPDATE_SECTION_NAME) {
                    runBlocking { fhirEngine.update(*resources.toTypedArray()) }
                }
            }
        }

    suspend fun traceGetResource(resourceType: ResourceType, resourceId: String) =
        withContext(tracingDispatcher) {
            measureTime {
                trace(TRACE_GET_SECTION_NAME) {
                    runBlocking { fhirEngine.get(resourceType, resourceId) }
                }
            }
        }

    suspend fun traceDeleteResources(resourceType: ResourceType, resourceId: String) =
        withContext(tracingDispatcher) {
            measureTime {
                trace(TRACE_DELETE_SECTION_NAME) {
                    runBlocking { fhirEngine.delete(resourceType, resourceId) }
                }
            }
        }

    fun traceDataAccess() {
        viewModelScope.launch {
            val dataAccessor = BulkDataAccessor(assetManager, fhirR4JsonParser)
            // Create
            createMutableStateFlow.update { CreateUiState.Loading }
            fhirEngine.clearDatabase()
            try {
                dataAccessor.fetchBulkResources()
                    .chunked(PROCESS_RESOURCES_LIMIT)
                    .forEach { resources ->
                        val timeTaken = traceCreateResources(resources)
                        createMutableStateFlow.update {
                            CreateUiState.Result(
                                Pair(
                                    resources.size,
                                    timeTaken
                                )
                            )
                        }
                    }
            } finally {
                dataAccessor.closeReaders()
            }

            // Get

            // Update

            // Delete
        }

    }

    companion object {
        private const val PROCESS_RESOURCES_LIMIT = 1000

        const val TRACE_CREATE_SECTION_NAME = "Create API"
        const val TRACE_UPDATE_SECTION_NAME = "Update API"
        const val TRACE_GET_SECTION_NAME = "Get API"
        const val TRACE_DELETE_SECTION_NAME = "Delete API"
    }

}

class MainViewModelFactory(private val application: Application, private val fhirEngine: FhirEngine): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(application, fhirEngine) as T
        }
        return super.create(modelClass)
    }
}

sealed interface CreateUiState {
    data object Loading: CreateUiState

    data class Result(val value: Pair<Int, Duration>): CreateUiState
}
