package com.fieldvet

import android.app.Application
import com.fieldvet.model.InferenceEngine
import com.fieldvet.model.KnowledgeBase

class FieldVetApplication : Application() {
    val knowledgeBase: KnowledgeBase by lazy { KnowledgeBase(this).apply { seedIfEmpty() } }
    val inferenceEngine: InferenceEngine by lazy { InferenceEngine(this) }
}
