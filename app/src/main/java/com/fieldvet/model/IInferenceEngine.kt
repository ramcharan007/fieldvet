package com.fieldvet.model

import kotlinx.coroutines.flow.Flow

interface IInferenceEngine {
    fun generateResponseStream(prompt: String): Flow<String>
}
