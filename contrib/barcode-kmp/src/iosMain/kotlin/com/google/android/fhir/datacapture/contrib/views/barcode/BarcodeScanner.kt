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

package com.google.android.fhir.datacapture.contrib.views.barcode

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication

/**
 * iOS implementation for launching and interacting with the Vision framework barcode scanning
 * capabilities.
 */
actual class BarcodeScanner {
  actual suspend fun scanBarcode(): String? = suspendCancellableCoroutine { continuation ->
    val window = UIApplication.sharedApplication.keyWindow
    val rootViewController = window?.rootViewController

    if (rootViewController != null) {
      val scannerViewController = ScannerViewController { result -> continuation.resume(result) }
      rootViewController.presentViewController(
        scannerViewController,
        animated = true,
        completion = null,
      )
    } else {
      continuation.resume(null)
    }
  }
}

@Composable
actual fun getBarcodeScanner(): BarcodeScanner {
  TODO("Not yet implemented")
}
