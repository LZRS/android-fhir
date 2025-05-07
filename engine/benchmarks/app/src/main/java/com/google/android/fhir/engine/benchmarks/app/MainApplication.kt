package com.google.android.fhir.engine.benchmarks.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.fhir.DatabaseErrorStrategy.RECREATE_AT_OPEN
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.NetworkConfiguration
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.sync.remote.HttpLogger

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                RECREATE_AT_OPEN,
                ServerConfiguration(
                    "https://hapi.fhir.org/baseR4/",
                    httpLogger =
                        HttpLogger(
                            HttpLogger.Configuration(
                                HttpLogger.Level.BODY,
                            ),
                        ) {
                            Log.i(TAG, "App-HttpLog")
                            Log.i(TAG, it)
                        },
                    networkConfiguration = NetworkConfiguration(uploadWithGzip = false),
                ),
            ),
        )
    }

    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    // Only initiate the FhirEngine when used for the first time, not when the app is created.
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }


    companion object {
        private val TAG = MainApplication::class.java.simpleName
        fun fhirEngine(context: Context) = (context.applicationContext as MainApplication).fhirEngine
    }
}