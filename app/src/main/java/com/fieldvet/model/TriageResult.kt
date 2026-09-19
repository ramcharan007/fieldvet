package com.fieldvet.model

data class TriageResult(
    val isSuccess: Boolean,
    val responseText: String? = null,
    val urgencyLevel: String? = null,
    val sourceCitation: String? = null,
    val errorMessage: String? = null,
)
