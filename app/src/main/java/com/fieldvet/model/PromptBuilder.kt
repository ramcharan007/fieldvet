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
            You are a veterinary triage assistant. Answer using ONLY the reference
            information below. Do not invent facts that aren't in it. Be concise.

            Species: $species
            Symptoms: $symptoms

            Reference information:
            $referenceBlock

            Give the farmer short, practical advice based only on the reference above.
        """.trimIndent()
    }
}
