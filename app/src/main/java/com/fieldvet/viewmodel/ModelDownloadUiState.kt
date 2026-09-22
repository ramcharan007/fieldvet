package com.fieldvet.viewmodel

sealed interface ModelDownloadUiState {
    data object Checking : ModelDownloadUiState
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : ModelDownloadUiState
    data object Verifying : ModelDownloadUiState
    data object Ready : ModelDownloadUiState
    data class Error(val message: String) : ModelDownloadUiState
}
