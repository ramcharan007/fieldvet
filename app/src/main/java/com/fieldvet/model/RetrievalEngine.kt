package com.fieldvet.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RetrievalEngine(private val knowledgeBase: KnowledgeBase) : IRetrievalEngine {

    override suspend fun retrieveRelevantEntries(species: String, symptoms: String): List<KnowledgeEntry> {
        return withContext(Dispatchers.IO) {
            knowledgeBase.search(buildMatchQuery(species, symptoms))
        }
    }

    /**
     * Builds an FTS5 MATCH query like: species:"cattle" AND ("distended" OR "abdomen" OR "kicking")
     * Species must match; any single symptom keyword is enough to surface a candidate,
     * and FTS5's bm25 ranking (see KnowledgeBase.search) then sorts best matches first.
     */
    private fun buildMatchQuery(species: String, symptoms: String): String {
        val speciesClause = "species:\"${sanitize(species)}\""

        val symptomTerms = symptoms
            .split(Regex("\\s+"))
            .map { sanitize(it) }
            .filter { it.isNotBlank() }

        if (symptomTerms.isEmpty()) return speciesClause

        val symptomsClause = symptomTerms.joinToString(" OR ") { "\"$it\"" }
        return "$speciesClause AND ($symptomsClause)"
    }

    private fun sanitize(token: String): String = token.filter { it.isLetterOrDigit() }
}
