package com.fieldvet.model

sealed interface GroundingResult {
    data class Grounded(val urgencyLevel: String, val sourceCitation: String, val prompt: String) : GroundingResult
    data object NoMatch : GroundingResult
}
