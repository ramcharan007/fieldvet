package com.fieldvet.model

class UrgencyClassifier {
    fun classify(entry: KnowledgeEntry): String = entry.urgency
}
