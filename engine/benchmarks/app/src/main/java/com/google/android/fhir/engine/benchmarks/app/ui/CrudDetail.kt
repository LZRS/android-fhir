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

package com.google.android.fhir.engine.benchmarks.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.fhir.engine.benchmarks.app.BenchmarkDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun CrudDetail(
  createStateFlow: StateFlow<List<BenchmarkDuration>>,
  getStateFlow: StateFlow<List<BenchmarkDuration>>,
  updateStateFlow: StateFlow<List<BenchmarkDuration>>,
  deleteStateFlow: StateFlow<List<BenchmarkDuration>>,
  runBenchmark: () -> Unit,
  navigateToHome: () -> Unit,
) {
  val createUiState = createStateFlow.collectAsStateWithLifecycle()
  val getUiState = getStateFlow.collectAsStateWithLifecycle()
  val updateUiState = updateStateFlow.collectAsStateWithLifecycle()
  val deleteUiState = deleteStateFlow.collectAsStateWithLifecycle()

  LaunchedEffect(true) { runBenchmark() }

  DetailScaffold("CRUD", navigateToHome) {
    Card(
      shape = RoundedCornerShape(10.dp),
      colors =
        CardDefaults.cardColors(
          containerColor = Color.White,
        ),
      modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)){
        if (createUiState.value.isNotEmpty()) {
          Column(Modifier.padding(8.dp)) {
            Text("Create API")
            Spacer(Modifier.size(8.dp))
            FlowRow {
              createUiState.value.forEach {
                Column {
                  Text("${it.first}")
                  Text("${it.second}")
                }
                Spacer(Modifier.size(8.dp))
              }
            }
          }
        } else {
          // Show loading
        }

        if (getUiState.value.isNotEmpty()) {
          Column(Modifier.padding(8.dp)) {
            Text("Get API")
            Spacer(Modifier.size(8.dp))
            FlowRow {
              getUiState.value.forEach {
                Column {
                  Text("${it.first}")
                  Text("${it.second}")
                }
                Spacer(Modifier.size(8.dp))
              }
            }
          }
        } else {
          // Show loading
        }

        if (updateUiState.value.isNotEmpty()) {
          Column(Modifier.padding(8.dp)) {
            Text("Update API")
            Spacer(Modifier.size(8.dp))
            FlowRow {
              updateUiState.value.forEach {
                Column {
                  Text("${it.first}")
                  Text("${it.second}")
                }
                Spacer(Modifier.size(8.dp))
              }
            }
          }
        } else {
          // Show loading
        }

        if (deleteUiState.value.isNotEmpty()) {
          Column(Modifier.padding(8.dp)) {
            Text("Delete API")
            Spacer(Modifier.size(8.dp))
            FlowRow {
              deleteUiState.value.forEach {
                Column {
                  Text("${it.first}")
                  Text("${it.second}")
                }
                Spacer(Modifier.size(8.dp))
              }
            }
          }
        } else {
          // Show loading
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewCrudDetail() {
  CrudDetail(
    MutableStateFlow(listOf(BenchmarkDuration(200, 4.seconds))),
    MutableStateFlow(listOf(BenchmarkDuration(0, 30.milliseconds))),
    MutableStateFlow(listOf(BenchmarkDuration(1, 240.milliseconds))),
    MutableStateFlow(listOf(BenchmarkDuration(0, 1.milliseconds))),
    {}) {}
}
