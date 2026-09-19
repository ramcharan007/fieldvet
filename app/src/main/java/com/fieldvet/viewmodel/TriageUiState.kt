package com.fieldvet.viewmodel

import com.fieldvet.model.TriageResult

sealed interface TriageUiState {
    data object Idle : TriageUiState
    data object Loading : TriageUiState
    data class Success(val result: TriageResult) : TriageUiState
    data class Error(val message: String) : TriageUiState
}
