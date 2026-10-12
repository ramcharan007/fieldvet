package com.fieldvet.model

import android.util.Log
import kotlinx.coroutines.flow.Flow

class TriageUseCase(
    private val retrievalEngine: IRetrievalEngine,
    private val inferenceEngine: IInferenceEngine,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val urgencyClassifier: UrgencyClassifier = UrgencyClassifier(),
) {

    suspend fun resolveGrounding(species: String, symptomText: String): GroundingResult {

        val entries = retrievalEngine.retrieveRelevantEntries(species, symptomText)

        if (entries.isEmpty()) {
            return GroundingResult.NoMatch
        }

        val topEntry = entries.first()
        val prompt = promptBuilder.buildPrompt(species, symptomText, entries)

        return GroundingResult.Grounded(
            urgencyLevel = urgencyClassifier.classify(topEntry),
            prompt = prompt,
        )
    }

    fun streamGuidance(prompt: String): Flow<String> = inferenceEngine.generateResponseStream(prompt)
}
