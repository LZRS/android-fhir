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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.fhir.engine.benchmarks.app.ui.CrudDetail
import com.google.android.fhir.engine.benchmarks.app.ui.Home
import com.google.android.fhir.engine.benchmarks.app.ui.Screen
import com.google.android.fhir.engine.benchmarks.app.ui.SearchApiDetail
import com.google.android.fhir.engine.benchmarks.app.ui.SyncApiDetail
import com.google.android.fhir.engine.benchmarks.app.ui.theme.AndroidfhirTheme

class MainActivity : ComponentActivity() {

  private val viewModel by lazy {
    val fhirEngine = MainApplication.fhirEngine(this@MainActivity)
    ViewModelProvider(
      this@MainActivity,
      MainViewModelFactory(application, fhirEngine),
    )[MainViewModel::class.java]
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { AndroidfhirTheme { AndroidfhirApp(viewModel) } }
  }
}

@Composable
fun AndroidfhirApp(viewModel: MainViewModel) {
  val navController = rememberNavController()
  NavHost(navController, startDestination = Screen.HomeScreen) {
    composable<Screen.HomeScreen> {
      Home(
        { navController.navigate(Screen.CRUDDetailScreen) },
        { navController.navigate(Screen.SearchDetailScreen) },
        {
          navController.navigate(
            Screen.SyncDetailScreen,
          )
        },
      )
    }

    composable<Screen.CRUDDetailScreen> {
      CrudDetail(viewModel.createStateFlow, viewModel.getStateFlow, viewModel.updateStateFlow, viewModel.deleteStateFlow, { viewModel.traceDataAccess() }) {
        navController.popBackStack()
      }
    }

    composable<Screen.SearchDetailScreen> { SearchApiDetail { navController.popBackStack() } }

    composable<Screen.SyncDetailScreen> { SyncApiDetail { navController.popBackStack() } }
  }
}
