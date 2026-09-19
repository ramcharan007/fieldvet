package com.fieldvet.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "FieldVetInference"


@RunWith(AndroidJUnit4::class)
class InferenceEngineInstrumentedTest {

    @Test
    fun generateResponse_cattleBloatPrompt_logsResult() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inferenceEngine: IInferenceEngine = InferenceEngine(context)

        val prompt = "A cow has a distended abdomen and is kicking at its belly. What might be wrong?"
        Log.d(TAG, "Prompt: $prompt")

        val modelPushed = java.io.File("/data/local/tmp/llm/model.litertlm").exists()
        if (!modelPushed) {
            val exception = assertThrows(InferenceException::class.java) {
                runBlocking { inferenceEngine.generateResponse(prompt) }
            }
            Log.d(TAG, "Expected failure (model not pushed): ${exception.message}")
            return@runBlocking
        }

        val response = inferenceEngine.generateResponse(prompt)
        Log.d(TAG, "Response: $response")
        assertTrue("Expected a non-blank response", response.isNotBlank())
    }
}
