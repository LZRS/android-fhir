package com.google.android.fhir.engine.benchmarks.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.fhir.engine.benchmarks.app.CreateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CrudDetail(createUiStateFlow: StateFlow<CreateUiState>, runBenchmark: () -> Unit, navigateToHome: () -> Unit) {
    val createUiState = createUiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        runBenchmark()
    }

    DetailScaffold("CRUD", navigateToHome) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when(createUiState.value) {
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
    CrudDetail(MutableStateFlow(CreateUiState.Result(Pair(1000, 3000.milliseconds))), {}) { }
}