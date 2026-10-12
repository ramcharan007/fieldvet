package com.fieldvet.model

import android.content.Context
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LiteRtLmJniException
import com.google.ai.edge.litertlm.LogSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val MAX_OUTPUT_TOKENS = 300

class InferenceEngine(private val context: Context) : IInferenceEngine {

    private val initMutex = Mutex()
    private val inferenceMutex = Mutex()
    private var engine: Engine? = null

    // Model loading/inference is CPU-bound native work, not blocking I/O, so this
    // uses Dispatchers.Default rather than the Dispatchers.IO used by RetrievalEngine.
    // flowOn keeps ensureInitialized()'s blocking native init (and the sendMessageAsync
    // collection) off Main, since this flow would otherwise run on the collector's
    // dispatcher (viewModelScope -> Dispatchers.Main.immediate).
    //
    // The Engine (loaded model) is reused, but each call gets a fresh Conversation:
    // a Conversation accumulates every prior turn in its history/KV cache, so reusing
    // one grows native memory per query and leaks earlier queries' context into later
    // answers, breaking grounding.
    override fun generateResponseStream(prompt: String): Flow<String> = flow {
        val eng = ensureInitialized()
        inferenceMutex.withLock {
            val conv = try {
                eng.createConversation()
            } catch (e: LiteRtLmJniException) {
                throw InferenceException("LiteRT-LM failed to create conversation: ${e.message}", e)
            }
            var completed = false
            try {
                val accumulated = StringBuilder()
                conv.sendMessageAsync(prompt, maxOutputToken = MAX_OUTPUT_TOKENS).collect { message ->
                    val chunk = message.contents.contents
                        .filterIsInstance<Content.Text>()
                        .joinToString("") { it.text }
                    accumulated.append(chunk)
                    emit(accumulated.toString())
                }
                completed = true
            } catch (e: LiteRtLmJniException) {
                throw InferenceException("LiteRT-LM failed to run inference: ${e.message}", e)
            } finally {
                if (!completed) conv.cancelProcess()
                conv.close()
            }
        }
    }.flowOn(Dispatchers.Default)

    private suspend fun ensureInitialized(): Engine = initMutex.withLock {
        engine?.let { return@withLock it }

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

        engine = newEngine
        newEngine
    }
}
