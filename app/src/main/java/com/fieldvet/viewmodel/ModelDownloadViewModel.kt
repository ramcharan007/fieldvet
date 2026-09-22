package com.fieldvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.WorkInfo
import com.fieldvet.model.KEY_BYTES_DOWNLOADED
import com.fieldvet.model.KEY_ERROR_MESSAGE
import com.fieldvet.model.KEY_STAGE
import com.fieldvet.model.KEY_TOTAL_BYTES
import com.fieldvet.model.ModelDownloadManager
import com.fieldvet.model.STAGE_VERIFYING
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModelDownloadViewModel(private val downloadManager: ModelDownloadManager) : ViewModel() {

    private val _uiState = MutableStateFlow<ModelDownloadUiState>(ModelDownloadUiState.Checking)
    val uiState: StateFlow<ModelDownloadUiState> = _uiState.asStateFlow()

    init {
        start()
    }

    private fun start() {
        viewModelScope.launch {
            if (downloadManager.isModelReady()) {
                _uiState.value = ModelDownloadUiState.Ready
                return@launch
            }

            downloadManager.enqueueDownload()
            downloadManager.observeWork().collect { workInfo ->
                _uiState.value = workInfo.toUiState()
            }
        }
    }

    fun retry() {
        _uiState.value = ModelDownloadUiState.Checking
        downloadManager.retry()
    }

    private fun WorkInfo?.toUiState(): ModelDownloadUiState = when (this?.state) {
        null, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> ModelDownloadUiState.Checking
        WorkInfo.State.RUNNING -> progress.toDownloadUiState()
        WorkInfo.State.SUCCEEDED -> ModelDownloadUiState.Ready
        WorkInfo.State.FAILED ->
            ModelDownloadUiState.Error(outputData.getString(KEY_ERROR_MESSAGE) ?: "Download failed. Please retry.")
        WorkInfo.State.CANCELLED -> ModelDownloadUiState.Error("Download cancelled. Please retry.")
    }

    private fun Data.toDownloadUiState(): ModelDownloadUiState {
        if (getString(KEY_STAGE) == STAGE_VERIFYING) return ModelDownloadUiState.Verifying
        val bytesDownloaded = getLong(KEY_BYTES_DOWNLOADED, -1L)
        val totalBytes = getLong(KEY_TOTAL_BYTES, -1L)
        return if (bytesDownloaded >= 0 && totalBytes > 0) {
            ModelDownloadUiState.Downloading(bytesDownloaded, totalBytes)
        } else {
            ModelDownloadUiState.Checking
        }
    }
}
