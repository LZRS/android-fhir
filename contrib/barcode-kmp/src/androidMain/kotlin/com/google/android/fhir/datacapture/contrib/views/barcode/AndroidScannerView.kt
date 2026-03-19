/*
 * Copyright 2026 Google LLC
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

import androidx.activity.result.contract.ActivityResultContracts
// import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.DisposableEffect
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.platform.LocalLifecycleOwner
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.viewinterop.AndroidView
// import androidx.lifecycle.Lifecycle
// import androidx.lifecycle.LifecycleEventObserver
// import androidx.lifecycle.viewmodel.compose.viewModel
// import com.google.android.fhir.datacapture.contrib.views.barcode.BarcodeProcessor
// import com.google.android.fhir.datacapture.contrib.views.barcode.CameraSource
// import com.google.android.fhir.datacapture.contrib.views.barcode.CameraSourcePreview
// import com.google.android.fhir.datacapture.contrib.views.barcode.GraphicOverlay
// import com.google.android.fhir.datacapture.contrib.views.barcode.WorkflowModel
//
// @Composable
// internal fun AndroidScannerView(
//  onScanned: (String) -> Unit,
// ) {
//  val context = LocalContext.current
//  val lifecycleOwner = LocalLifecycleOwner.current
//
//  var hasCameraPermission by remember { mutableStateOf(false) }
//
//  val permissionLauncher =
//    rememberLauncherForActivityResult(
//      ActivityResultContracts.RequestPermission(),
//    ) { isGranted ->
//      hasCameraPermission = isGranted
//    }
//
//  LaunchedEffect(Unit) { permissionLauncher.launch(android.Manifest.permission.CAMERA) }
//
//  if (hasCameraPermission) {
//    var cameraSource by remember { mutableStateOf<CameraSource?>(null) }
//    val graphicOverlay = remember { GraphicOverlay(context, null) }
//    val workflowModel: WorkflowModel =
//      viewModel(
//        factory =
//          androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
//            context.applicationContext as Application,
//          ),
//      )
//
//    DisposableEffect(lifecycleOwner) {
//      val observer = LifecycleEventObserver { _, event ->
//        if (event == Lifecycle.Event.ON_RESUME) {
//          cameraSource =
//            CameraSource(graphicOverlay).apply {
//              setFrameProcessor(BarcodeProcessor(graphicOverlay, workflowModel))
//            }
//          workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
//        } else if (event == Lifecycle.Event.ON_PAUSE) {
//          cameraSource?.release()
//          cameraSource = null
//        } else if (event == Lifecycle.Event.ON_DESTROY) {
//          cameraSource?.release()
//          cameraSource = null
//        }
//      }
//      lifecycleOwner.lifecycle.addObserver(observer)
//      onDispose {
//        lifecycleOwner.lifecycle.removeObserver(observer)
//        cameraSource?.release()
//      }
//    }
//
//    LaunchedEffect(workflowModel.detectedBarcode) {
//      workflowModel.detectedBarcode.observe(lifecycleOwner) { barcode ->
//        if (barcode != null && barcode.rawValue != null) {
//          onScanned(barcode.rawValue!!)
//        }
//      }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//      AndroidView(
//        factory = { ctx ->
//          val preview = CameraSourcePreview(ctx, null)
//          preview.layoutParams =
//            ViewGroup.LayoutParams(
//              ViewGroup.LayoutParams.MATCH_PARENT,
//              ViewGroup.LayoutParams.MATCH_PARENT,
//            )
//
//          graphicOverlay.layoutParams =
//            ViewGroup.LayoutParams(
//              ViewGroup.LayoutParams.MATCH_PARENT,
//              ViewGroup.LayoutParams.MATCH_PARENT,
//            )
//
//          val container =
//            FrameLayout(ctx).apply {
//              addView(preview)
//              addView(graphicOverlay)
//            }
//
//          try {
//            cameraSource?.let { preview.start(it, graphicOverlay) }
//          } catch (e: Exception) {
//            e.printStackTrace()
//          }
//
//          container
//        },
//        modifier = Modifier.fillMaxSize(),
//      )
//
//      // Optional: Prompt chip
//      Text(
//        text = "Point at a barcode",
//        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
//        color = androidx.compose.ui.graphics.Color.White,
//      )
//    }
//  } else {
//    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
//      Text(
//        "Requesting camera permission...",
//        color = androidx.compose.ui.graphics.Color.White,
//        modifier = Modifier.align(Alignment.Center),
//      )
//    }
//  }
// }
