package com.fieldvet.model

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "FieldVetRetrieval"

/**
 * Manual verification harness for Checkpoint B: seeds the knowledge base (if
 * empty) and runs a sample retrieval query, logging results to Logcat so they
 * can be inspected without any UI. Run via `./gradlew connectedAndroidTest`
 * (or the gutter run icon) on a connected device/emulator, then filter Logcat
 * by the tag "FieldVetRetrieval".
 */
@RunWith(AndroidJUnit4::class)
class RetrievalEngineInstrumentedTest {

    @Test
    fun retrieveRelevantEntries_cattleBloatSample_logsResults() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val knowledgeBase = KnowledgeBase(context)
        knowledgeBase.seedIfEmpty()

        val retrievalEngine: IRetrievalEngine = RetrievalEngine(knowledgeBase)
        val species = "cattle"
        val symptoms = "distended abdomen kicking"
        val results = retrievalEngine.retrieveRelevantEntries(species, symptoms)

        Log.d(TAG, "Query: species=$species, symptoms='$symptoms' -> ${results.size} result(s)")
        results.forEachIndexed { index, entry ->
            Log.d(
                TAG,
                "[$index] id=${entry.id} condition=${entry.condition} urgency=${entry.urgency} " +
                    "symptoms=${entry.symptoms} actionText=${entry.actionText}"
            )
        }

        assertTrue("Expected at least one match for cattle bloat symptoms", results.isNotEmpty())
    }
}
