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
            MainViewModelFactory(application, fhirEngine)
        )[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidfhirTheme {
                AndroidfhirApp(viewModel)
            }
        }
    }
}

@Composable
fun AndroidfhirApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.HomeScreen) {
        composable<Screen.HomeScreen> {
            Home(
                { navController.navigate(Screen.CRUDDetailScreen) },
                {
                    navController.navigate(Screen.SearchDetailScreen)
                },
                {
                    navController.navigate(
                        Screen.SyncDetailScreen
                    )
                })
        }

        composable<Screen.CRUDDetailScreen> {
            CrudDetail(viewModel.createStateFlow, {viewModel.traceDataAccess()}) { navController.popBackStack() }
        }

        composable<Screen.SearchDetailScreen> {
            SearchApiDetail { navController.popBackStack() }
        }

        composable<Screen.SyncDetailScreen> {
            SyncApiDetail { navController.popBackStack() }
        }
    }
}
