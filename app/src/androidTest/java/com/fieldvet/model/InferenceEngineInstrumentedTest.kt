package com.fieldvet.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
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
        println("Starting inference")
        Log.i(TAG, "Starting inference for prompt: $prompt")
        
        val response = inferenceEngine.generateResponse(prompt)

        println("-----------INFERENCE RESULT -------")
        println("Prompt: $prompt")
        println("Response: $response")
        println("--------------------------------------")
        
        Log.i(TAG, "Prompt: $prompt")
        Log.i(TAG, "Response: $response")

        assertTrue("Expected a non-blank response", response.isNotBlank())
    }
}
