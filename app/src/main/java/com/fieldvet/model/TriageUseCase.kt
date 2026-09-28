package com.fieldvet.model

import android.util.Log
import kotlinx.coroutines.flow.Flow

private const val TAG = "FieldVetTriage"

class TriageUseCase(
    private val retrievalEngine: IRetrievalEngine,
    private val inferenceEngine: IInferenceEngine,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val urgencyClassifier: UrgencyClassifier = UrgencyClassifier(),
) {

    suspend fun resolveGrounding(species: String, symptomText: String): GroundingResult {
        val t0 = System.currentTimeMillis()
        val entries = retrievalEngine.retrieveRelevantEntries(species, symptomText)
        val t1 = System.currentTimeMillis()
        Log.d(TAG, "retrieval: ${t1 - t0}ms, ${entries.size} match(es)")

        if (entries.isEmpty()) {
            return GroundingResult.NoMatch
        }

        val topEntry = entries.first()
        val prompt = promptBuilder.buildPrompt(species, symptomText, entries)
        Log.d(TAG, "promptBuild: ${System.currentTimeMillis() - t1}ms")

        return GroundingResult.Grounded(
            urgencyLevel = urgencyClassifier.classify(topEntry),
            sourceCitation = topEntry.sourceCitation,
            prompt = prompt,
        )
    }

    fun streamGuidance(prompt: String): Flow<String> = inferenceEngine.generateResponseStream(prompt)
}
