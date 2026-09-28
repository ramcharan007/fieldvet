package com.fieldvet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldvet.model.GroundingResult
import com.fieldvet.model.InferenceException
import com.fieldvet.model.TriageResult
import com.fieldvet.model.TriageUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "FieldVetTriage"

class TriageViewModel(private val triageUseCase: TriageUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<TriageUiState>(TriageUiState.Idle)
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    private val _streamState = MutableStateFlow(GuidanceStreamState())
    val streamState: StateFlow<GuidanceStreamState> = _streamState.asStateFlow()

    private val _submittedSymptomText = MutableStateFlow("")
    val submittedSymptomText: StateFlow<String> = _submittedSymptomText.asStateFlow()

    private var lastPrompt: String? = null
    private var groundingJob: Job? = null
    private var streamingJob: Job? = null

    fun onSymptomSubmitted(species: String, symptomText: String) {
        groundingJob?.cancel()
        streamingJob?.cancel()
        _submittedSymptomText.value = symptomText
        _uiState.value = TriageUiState.Loading
        _streamState.value = GuidanceStreamState()
        groundingJob = viewModelScope.launch {
            try {
                when (val grounding = triageUseCase.resolveGrounding(species, symptomText)) {
                    is GroundingResult.NoMatch -> _uiState.value = TriageUiState.NoMatch
                    is GroundingResult.Grounded -> {
                        lastPrompt = grounding.prompt
                        _uiState.value = TriageUiState.Success(
                            TriageResult(grounding.urgencyLevel, grounding.sourceCitation),
                        )
                        startStreaming(grounding.prompt)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TriageUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    private fun startStreaming(prompt: String) {
        streamingJob?.cancel()
        val startedAt = System.currentTimeMillis()
        streamingJob = viewModelScope.launch {
            _streamState.value = _streamState.value.copy(isStreaming = true, error = null)
            triageUseCase.streamGuidance(prompt)
                .onEach { text -> _streamState.value = _streamState.value.copy(text = text) }
                .onCompletion { cause ->
                    Log.d(TAG, "inference: ${System.currentTimeMillis() - startedAt}ms")
                    if (cause == null) {
                        _streamState.value = _streamState.value.copy(isStreaming = false)
                    }
                }
                .catch { e ->
                    // Flow.catch never intercepts CancellationException, so this is safe as-is.
                    val message = (e as? InferenceException)?.message ?: e.message ?: "Something went wrong"
                    _streamState.value = _streamState.value.copy(isStreaming = false, error = message)
                }
                .collect()
        }
    }

    fun retryStreaming() {
        lastPrompt?.let { startStreaming(it) }
    }

    fun reset() {
        groundingJob?.cancel()
        streamingJob?.cancel()
        _uiState.value = TriageUiState.Idle
        _streamState.value = GuidanceStreamState()
    }
}
