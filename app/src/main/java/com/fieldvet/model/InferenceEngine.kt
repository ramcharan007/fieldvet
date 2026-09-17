package com.fieldvet.model

import android.content.Context
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LiteRtLmJniException
import com.google.ai.edge.litertlm.LogSeverity
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MODEL_PATH = "/data/local/tmp/llm/model.litertlm"

class InferenceEngine(private val context: Context) : IInferenceEngine {

    // Model loading/inference is CPU-bound native work, not blocking I/O, so this
    // uses Dispatchers.Default rather than the Dispatchers.IO used by RetrievalEngine.
    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.Default) {
        val modelFile = File(MODEL_PATH)
        if (!modelFile.exists()) {
            return@withContext "[InferenceEngine error] Model file not found at $MODEL_PATH. " +
                "Push it via: adb push <model-file> $MODEL_PATH"
        }

        try {

            Engine(EngineConfig(modelPath = MODEL_PATH, cacheDir = context.cacheDir.path)).use { engine ->
                engine.initialize()
                engine.createConversation().use { conversation ->
                    val message = conversation.sendMessage(prompt)
                    message.contents.contents
                        .filterIsInstance<Content.Text>()
                        .joinToString("") { it.text }
                }
            }
        } catch (e: LiteRtLmJniException) {
            "[InferenceEngine error] LiteRT-LM failed to run inference: ${e.message}"
        } catch (e: Exception) {
            "[InferenceEngine error] Unexpected failure during inference: ${e.message}"
        }
    }
}
