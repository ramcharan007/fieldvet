package com.fieldvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldvet.model.ModelDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination {
    ModelSetup,
    SpeciesSelect,
}

class SplashViewModel(private val downloadManager: ModelDownloadManager) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // isModelReady() wraps its file I/O in Dispatchers.IO internally, so this
            // never blocks the main thread despite running from a plain launch{}.
            val ready = downloadManager.isModelReady()
            _destination.value = if (ready) SplashDestination.SpeciesSelect else SplashDestination.ModelSetup
        }
    }
}
