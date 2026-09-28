package com.fieldvet.model

private const val TOP_N_ENTRIES = 2

class PromptBuilder {

    fun buildPrompt(species: String, symptoms: String, entries: List<KnowledgeEntry>): String {
        val referenceBlock = entries.take(TOP_N_ENTRIES).joinToString("\n\n") { entry ->
            "Condition: ${entry.condition}\n" +
                "Advice: ${entry.actionText}\n" +
                "Source: ${entry.sourceCitation}"
        }

        return """
            You are a veterinary triage assistant helping a farmer in the field. Answer using
            ONLY the reference information below - never state a fact, cause, or treatment that
            is not explicitly present in it. If the reference does not fully cover the symptoms
            described, say so plainly instead of filling the gap yourself.

            Species: $species
            Symptoms: $symptoms

            Reference information:
            $referenceBlock

            Write your answer as 3-5 short numbered steps the farmer should take right now, in
            the order they matter most. For each step, briefly explain why it matters using only
            what the reference says - do not add new causes, risks, or treatments beyond what is
            stated above. Keep the whole answer under about 120 words.
        """.trimIndent()
    }
}
