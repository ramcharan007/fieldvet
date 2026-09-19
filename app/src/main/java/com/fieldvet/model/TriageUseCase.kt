package com.fieldvet.model

import android.util.Log

private const val TAG = "FieldVetTriage"

class TriageUseCase(
    private val retrievalEngine: IRetrievalEngine,
    private val inferenceEngine: IInferenceEngine,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val urgencyClassifier: UrgencyClassifier = UrgencyClassifier(),
) {

    suspend fun runTriage(species: String, symptomText: String): TriageResult {
        val t0 = System.currentTimeMillis()
        val entries = retrievalEngine.retrieveRelevantEntries(species, symptomText)
        val t1 = System.currentTimeMillis()
        Log.d(TAG, "retrieval: ${t1 - t0}ms, ${entries.size} match(es)")

        if (entries.isEmpty()) {
            return TriageResult(
                isSuccess = false,
                errorMessage = "No relevant match found for these symptoms. Please contact a vet directly.",
            )
        }

        val topEntry = entries.first()
        val prompt = promptBuilder.buildPrompt(species, symptomText, entries)
        val t2 = System.currentTimeMillis()
        Log.d(TAG, "promptBuild: ${t2 - t1}ms")

        val responseText = inferenceEngine.generateResponse(prompt)
        val t3 = System.currentTimeMillis()
        Log.d(TAG, "inference: ${t3 - t2}ms")

        return TriageResult(
            isSuccess = true,
            responseText = responseText,
            urgencyLevel = urgencyClassifier.classify(topEntry),
            sourceCitation = topEntry.sourceCitation,
        )
    }
}
