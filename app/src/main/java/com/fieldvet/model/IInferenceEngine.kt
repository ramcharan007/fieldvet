package com.fieldvet.model

interface IInferenceEngine {
    suspend fun generateResponse(prompt: String): String
}
