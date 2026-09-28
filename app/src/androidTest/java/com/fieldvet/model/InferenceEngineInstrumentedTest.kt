package com.fieldvet.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "FieldVetInference"


@RunWith(AndroidJUnit4::class)
class InferenceEngineInstrumentedTest {

    @Test
    fun generateResponseStream_cattleBloatPrompt_logsResult() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inferenceEngine: IInferenceEngine = InferenceEngine(context)

        val prompt = "A cow has a distended abdomen and is kicking at its belly. What might be wrong?"
        Log.d(TAG, "Prompt: $prompt")

        val modelPushed = ModelStorage.modelFile(context).exists()
        if (!modelPushed) {
            val exception = assertThrows(InferenceException::class.java) {
                runBlocking { inferenceEngine.generateResponseStream(prompt).collect {} }
            }
            Log.d(TAG, "Expected failure (model not pushed): ${exception.message}")
            return@runBlocking
        }

        var lastEmission = ""
        inferenceEngine.generateResponseStream(prompt).collect { lastEmission = it }
        Log.d(TAG, "Response: $lastEmission")
        assertTrue("Expected a non-blank response", lastEmission.isNotBlank())
    }
}
