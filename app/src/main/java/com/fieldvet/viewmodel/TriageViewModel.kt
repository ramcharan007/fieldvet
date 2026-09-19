package com.fieldvet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldvet.model.InferenceException
import com.fieldvet.model.TriageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriageViewModel(private val triageUseCase: TriageUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<TriageUiState>(TriageUiState.Idle)
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    fun onSymptomSubmitted(species: String, symptomText: String) {
        _uiState.value = TriageUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                TriageUiState.Success(triageUseCase.runTriage(species, symptomText))
            } catch (e: InferenceException) {
                TriageUiState.Error(e.message ?: "Inference failed")
            } catch (e: Exception) {
                TriageUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun reset() {
        _uiState.value = TriageUiState.Idle
    }
}
