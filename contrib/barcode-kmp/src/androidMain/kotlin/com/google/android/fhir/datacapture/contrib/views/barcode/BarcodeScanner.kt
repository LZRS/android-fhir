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

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.fhir.datacapture.contrib.views.barcode.mlkit.md.LiveBarcodeScanningFragment
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android implementation for launching and interacting with the ML Kit barcode scanning
 * capabilities.
 */
actual class BarcodeScanner(private val context: Context) {

  actual suspend fun scanBarcode(): String? = suspendCancellableCoroutine {
    val activity = context.tryUnwrapContext()!!
    activity.supportFragmentManager.setFragmentResultListener(
      RESULT_REQUEST_KEY,
      activity,
    ) { _, result ->
      val barcode = result.getString(RESULT_REQUEST_KEY)?.trim()
      it.resumeWith(Result.success(barcode))
    }

    LiveBarcodeScanningFragment()
      .show(activity.supportFragmentManager, "LiveBarcodeScanningFragment")
  }

  private fun Context.tryUnwrapContext(): AppCompatActivity? {
    var context = this
    println("${context::class.qualifiedName}")
    while (true) {
      when (context) {
        is AppCompatActivity -> return context
        is ContextThemeWrapper -> context = context.baseContext
        else -> return null
      }
    }
  }

  companion object {
    const val RESULT_REQUEST_KEY =
      "com.google.android.fhir.datacapture.contrib.views.barcode.result"
  }
}

@Composable
actual fun getBarcodeScanner(): BarcodeScanner {
  val context = LocalContext.current
  return remember(context) { BarcodeScanner(context) }
}
