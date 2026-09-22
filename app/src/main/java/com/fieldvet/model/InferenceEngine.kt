package com.fieldvet.model

import android.content.Context
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LiteRtLmJniException
import com.google.ai.edge.litertlm.LogSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val MAX_OUTPUT_TOKENS = 200

class InferenceEngine(private val context: Context) : IInferenceEngine {

    private val initMutex = Mutex()
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    // Model loading/inference is CPU-bound native work, not blocking I/O, so this
    // uses Dispatchers.Default rather than the Dispatchers.IO used by RetrievalEngine.
    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.Default) {
        val conv = ensureInitialized()
        try {
            val message = conv.sendMessage(prompt, maxOutputToken = MAX_OUTPUT_TOKENS)
            message.contents.contents
                .filterIsInstance<Content.Text>()
                .joinToString("") { it.text }
        } catch (e: LiteRtLmJniException) {
            throw InferenceException("LiteRT-LM failed to run inference: ${e.message}", e)
        }
    }

    private suspend fun ensureInitialized(): Conversation = initMutex.withLock {
        conversation?.let { return@withLock it }

        val modelFile = ModelStorage.modelFile(context)
        if (!modelFile.exists()) {
            throw InferenceException("Model file not found at ${modelFile.absolutePath}.")
        }

        Engine.setNativeMinLogSeverity(LogSeverity.ERROR)

        val newEngine = Engine(EngineConfig(modelPath = modelFile.absolutePath, cacheDir = context.cacheDir.path))
        try {
            newEngine.initialize()
        } catch (e: Exception) {
            newEngine.close()
            throw InferenceException("Failed to initialize model: ${e.message}", e)
        }

        val newConversation = newEngine.createConversation()
        engine = newEngine
        conversation = newConversation
        newConversation
    }
}
