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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A KMP Compose wrapper for the Barcode Questionnaire Item.
 *
 * @param prefix Optional prefix text for the question.
 * @param questionText The actual question prompt.
 * @param initialBarcode The initially scanned barcode value, if any.
 * @param scanner The platform-specific scanner implementation.
 * @param onBarcodeScanned Callback invoked when a new barcode is scanned.
 */
@Composable
fun BarcodeQuestionnaireItem(
  prefix: String?,
  questionText: String,
  initialBarcode: String?,
  scanner: BarcodeScanner,
  onBarcodeScanned: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var scannedValue by remember { mutableStateOf(initialBarcode) }
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
    if (!prefix.isNullOrEmpty()) {
      Text(
        text = prefix,
        style = MaterialTheme.typography.labelSmall,
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    Text(
      text = questionText,
      style = MaterialTheme.typography.bodyLarge,
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = scannedValue ?: "",
      onValueChange = { /* Read only from user perspective, filled by scanner */},
      modifier = Modifier.fillMaxWidth(),
      readOnly = true,
      label = { Text("Barcode reading") },
      placeholder = { Text("No numeric reading") },
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
      onClick = {
        coroutineScope.launch {
          val result = scanner.scanBarcode()
          if (result != null) {
            scannedValue = result
            onBarcodeScanned(result)
          }
        }
      },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(if (scannedValue == null) "Scan Barcode" else "Re-scan Barcode")
    }
  }
}
