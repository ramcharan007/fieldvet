package com.fieldvet.model

data class KnowledgeEntry(
    val id: String,
    val species: String,
    val condition: String,
    val symptoms: List<String>,
    val urgency: String, // "Emergency", "Monitor", or "Non-urgent"
    val actionText: String,
    val sourceCitation: String
)
