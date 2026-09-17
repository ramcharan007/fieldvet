package com.fieldvet.model

interface IRetrievalEngine {
    suspend fun retrieveRelevantEntries(species: String, symptoms: String): List<KnowledgeEntry>
}
