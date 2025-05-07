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

import androidx.compose.foundation.layout.Column
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
import com.google.android.fhir.engine.benchmarks.app.CreateUiState
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CrudDetail(
  createUiStateFlow: StateFlow<CreateUiState>,
  runBenchmark: () -> Unit,
  navigateToHome: () -> Unit
) {
  val createUiState = createUiStateFlow.collectAsStateWithLifecycle()

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
      when (createUiState.value) {
        is CreateUiState.Loading -> {}
        is CreateUiState.Result -> {
          Column(Modifier.padding(8.dp)) {
            Text("Create API")
            Spacer(Modifier.size(8.dp))
            Text("${(createUiState.value as CreateUiState.Result).value.first}")
            Text("${(createUiState.value as CreateUiState.Result).value.second}")
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewCrudDetail() {
  CrudDetail(MutableStateFlow(CreateUiState.Result(Pair(1000, 3000.milliseconds))), {}) {}
}
