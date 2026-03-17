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

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
internal class ScannerViewController(
  private val onScanned: (String) -> Unit,
) : UIViewController(nibName = null, bundle = null) {

  private var captureSession: AVCaptureSession? = null
  private var previewLayer: AVCaptureVideoPreviewLayer? = null

  override fun viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = UIColor.blackColor

    checkPermissionsAndStart()
  }

  private fun checkPermissionsAndStart() {
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
      AVAuthorizationStatusAuthorized -> {
        setupCamera()
      }
      AVAuthorizationStatusNotDetermined -> {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
          dispatch_get_main_queue() // Ensure UI updates on main thread if needed
          if (granted) {
            setupCamera()
          } else {
            failed()
          }
        }
      }
      AVAuthorizationStatusDenied,
      AVAuthorizationStatusRestricted, -> {
        failed()
      }
      else -> failed()
    }
  }

  private fun setupCamera() {
    val captureSession = AVCaptureSession()
    this.captureSession = captureSession

    val videoCaptureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
    if (videoCaptureDevice == null) {
      failed()
      return
    }

    val videoInput =
      try {
        AVCaptureDeviceInput.deviceInputWithDevice(videoCaptureDevice, null)
      } catch (e: Exception) {
        null
      }

    if (videoInput != null && captureSession.canAddInput(videoInput)) {
      captureSession.addInput(videoInput)
    } else {
      failed()
      return
    }

    val metadataOutput = AVCaptureMetadataOutput()

    if (captureSession.canAddOutput(metadataOutput)) {
      captureSession.addOutput(metadataOutput)

      val delegate =
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
          override fun captureOutput(
            output: AVCaptureOutput,
            didOutputMetadataObjects: List<*>,
            fromConnection: AVCaptureConnection,
          ) {
            if (didOutputMetadataObjects.isNotEmpty()) {
              val metadataObject =
                didOutputMetadataObjects.first() as? AVMetadataMachineReadableCodeObject
              if (metadataObject != null && metadataObject.stringValue != null) {
                captureSession.stopRunning()
                dismissViewControllerAnimated(true, null)
                onScanned(metadataObject.stringValue!!)
              }
            }
          }
        }

      metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
      // Register for common barcode types
      metadataOutput.metadataObjectTypes =
        listOf(
          AVMetadataObjectTypeQRCode,
          AVMetadataObjectTypeEAN13Code,
        )
    } else {
      failed()
      return
    }

    previewLayer =
      AVCaptureVideoPreviewLayer(session = captureSession).apply {
        frame = view.layer.bounds
        videoGravity = AVLayerVideoGravityResizeAspectFill
      }

    view.layer.addSublayer(previewLayer!!)
    captureSession.startRunning()
  }

  override fun viewWillLayoutSubviews() {
    super.viewWillLayoutSubviews()
    previewLayer?.frame = view.frame
  }

  override fun viewWillAppear(animated: Boolean) {
    super.viewWillAppear(animated)
    if (captureSession?.isRunning() == false) {
      captureSession?.startRunning()
    }
  }

  override fun viewWillDisappear(animated: Boolean) {
    super.viewWillDisappear(animated)
    if (captureSession?.isRunning() == true) {
      captureSession?.stopRunning()
    }
  }

  private fun failed() {
    // Handle failure to init camera, e.g. simulator fallback
  }
}
