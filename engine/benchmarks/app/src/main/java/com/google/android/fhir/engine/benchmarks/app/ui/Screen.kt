package com.google.android.fhir.engine.benchmarks.app.ui

import kotlinx.serialization.Serializable


sealed interface Screen {
    @Serializable
    data object HomeScreen: Screen

    @Serializable
    data object CRUDDetailScreen: Screen

    @Serializable
    data object SearchDetailScreen: Screen

    @Serializable
    data object SyncDetailScreen: Screen
}
