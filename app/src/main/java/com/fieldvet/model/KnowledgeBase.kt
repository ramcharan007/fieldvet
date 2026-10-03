package com.fieldvet.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import org.json.JSONObject

private const val DATABASE_NAME = "fieldvet_knowledge.db"
// Bump whenever assets/knowledge_base.json changes: onUpgrade drops the table and
// seedIfEmpty() reloads it from the asset.
private const val DATABASE_VERSION = 2
private const val TABLE_NAME = "knowledge_entries"
private const val SYMPTOM_DELIMITER = " | "

class KnowledgeBase(context: Context) {

    private val appContext = context.applicationContext

    private val openHelper: SupportSQLiteOpenHelper = RequerySQLiteOpenHelperFactory().create(
        SupportSQLiteOpenHelper.Configuration.builder(context.applicationContext)
            .name(DATABASE_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(DATABASE_VERSION) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE VIRTUAL TABLE $TABLE_NAME USING fts5(
                            id UNINDEXED,
                            species,
                            condition,
                            symptoms,
                            urgency UNINDEXED,
                            actionText UNINDEXED,
                            sourceCitation UNINDEXED
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
                    onCreate(db)
                }
            })
            .build()
    )

    fun insertEntry(entry: KnowledgeEntry) {
        val values = ContentValues().apply {
            put("id", entry.id)
            put("species", entry.species)
            put("condition", entry.condition)
            put("symptoms", entry.symptoms.joinToString(SYMPTOM_DELIMITER))
            put("urgency", entry.urgency)
            put("actionText", entry.actionText)
            put("sourceCitation", entry.sourceCitation)
        }
        openHelper.writableDatabase.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, values)
    }

    /** Runs an FTS5 MATCH query, most relevant match first. */
    fun search(matchQuery: String): List<KnowledgeEntry> {
        val cursor = openHelper.readableDatabase.query(
            "SELECT id, species, condition, symptoms, urgency, actionText, sourceCitation " +
                "FROM $TABLE_NAME WHERE $TABLE_NAME MATCH ? ORDER BY rank",
            arrayOf(matchQuery)
        )
        return cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow("id")
            val speciesIndex = c.getColumnIndexOrThrow("species")
            val conditionIndex = c.getColumnIndexOrThrow("condition")
            val symptomsIndex = c.getColumnIndexOrThrow("symptoms")
            val urgencyIndex = c.getColumnIndexOrThrow("urgency")
            val actionTextIndex = c.getColumnIndexOrThrow("actionText")
            val sourceCitationIndex = c.getColumnIndexOrThrow("sourceCitation")

            val results = mutableListOf<KnowledgeEntry>()
            while (c.moveToNext()) {
                results += KnowledgeEntry(
                    id = c.getString(idIndex),
                    species = c.getString(speciesIndex),
                    condition = c.getString(conditionIndex),
                    symptoms = c.getString(symptomsIndex).split(SYMPTOM_DELIMITER),
                    urgency = c.getString(urgencyIndex),
                    actionText = c.getString(actionTextIndex),
                    sourceCitation = c.getString(sourceCitationIndex)
                )
            }
            results
        }
    }

    fun isEmpty(): Boolean {
        openHelper.readableDatabase.query("SELECT count(*) FROM $TABLE_NAME").use { c ->
            c.moveToFirst()
            return c.getInt(0) == 0
        }
    }

    /**
     * Seeds the bundled dataset (assets/knowledge_base.json) once. Safe to call on every
     * app launch. Runs in a single transaction so a partial seed is never left behind.
     */
    fun seedIfEmpty() {
        if (!isEmpty()) return
        val entries = loadBundledEntries()
        val db = openHelper.writableDatabase
        db.beginTransaction()
        try {
            entries.forEach { insertEntry(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun loadBundledEntries(): List<KnowledgeEntry> {
        val json = appContext.assets.open(SEED_ASSET_NAME).bufferedReader().use { it.readText() }
        val array = JSONObject(json).getJSONArray("entries")
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            val symptoms = obj.getJSONArray("symptoms")
            KnowledgeEntry(
                id = obj.getString("id"),
                species = obj.getString("species"),
                condition = obj.getString("condition"),
                symptoms = (0 until symptoms.length()).map { symptoms.getString(it) },
                urgency = obj.getString("urgency").also {
                    // Urgency is shown to the farmer verbatim, so a typo in the dataset must fail loudly.
                    require(it in VALID_URGENCIES) { "Invalid urgency '$it' for entry ${obj.getString("id")}" }
                },
                actionText = obj.getString("actionText"),
                sourceCitation = obj.getString("sourceCitation")
            )
        }
    }

    companion object {
        private const val SEED_ASSET_NAME = "knowledge_base.json"
        private val VALID_URGENCIES = setOf("Emergency", "Monitor", "Non-urgent")
    }
}
