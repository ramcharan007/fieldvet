package com.fieldvet.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory

private const val DATABASE_NAME = "fieldvet_knowledge.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "knowledge_entries"
private const val SYMPTOM_DELIMITER = " | "

class KnowledgeBase(context: Context) {

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

    /** Seeds illustrative starter content once. Safe to call on every app launch. */
    fun seedIfEmpty() {
        if (!isEmpty()) return
        SEED_ENTRIES.forEach { insertEntry(it) }
    }

    companion object {
        private val SEED_ENTRIES = listOf(
            KnowledgeEntry(
                id = "cattle_bloat_01",
                species = "cattle",
                condition = "Bloat",
                symptoms = listOf(
                    "distended abdomen",
                    "difficulty breathing",
                    "kicking at belly",
                    "restlessness"
                ),
                urgency = "Emergency",
                actionText = "Contact a veterinarian immediately. Do not delay - bloat can be " +
                    "fatal within hours. Keep the animal standing and moving if possible; " +
                    "avoid forcing it to lie down.",
                sourceCitation = "Merck Veterinary Manual - Bloat in Ruminants (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "cattle_dystocia_01",
                species = "cattle",
                condition = "Dystocia (Difficult Calving)",
                symptoms = listOf(
                    "prolonged straining",
                    "calf legs visible but not progressing",
                    "calf malpositioned",
                    "exhaustion"
                ),
                urgency = "Emergency",
                actionText = "Call a veterinarian immediately if active straining continues for " +
                    "more than 30 minutes without progress. Do not attempt manual traction " +
                    "without training.",
                sourceCitation = "Merck Veterinary Manual - Dystocia in Cattle (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "cattle_milkfever_01",
                species = "cattle",
                condition = "Milk Fever (Hypocalcemia)",
                symptoms = listOf(
                    "muscle tremors",
                    "down cow unable to stand",
                    "cold ears",
                    "staggering gait"
                ),
                urgency = "Emergency",
                actionText = "Contact a veterinarian immediately for IV calcium treatment. Keep " +
                    "the cow warm and upright with bedding support if possible.",
                sourceCitation = "Merck Veterinary Manual - Parturient Hypocalcemia (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "cattle_footrot_01",
                species = "cattle",
                condition = "Foot Rot",
                symptoms = listOf(
                    "lameness",
                    "swelling between hooves",
                    "foul odor",
                    "limping"
                ),
                urgency = "Monitor",
                actionText = "Isolate the animal, clean and inspect the hoof, and monitor for " +
                    "worsening. Schedule a veterinary visit if lameness persists beyond 2-3 " +
                    "days or spreads to other feet.",
                sourceCitation = "Merck Veterinary Manual - Foot Rot in Cattle (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "cattle_indigestion_01",
                species = "cattle",
                condition = "Mild Indigestion",
                symptoms = listOf(
                    "slightly reduced appetite",
                    "mild bloating",
                    "normal breathing",
                    "normal manure"
                ),
                urgency = "Non-urgent",
                actionText = "Monitor feed intake and manure over the next 24 hours. Ensure " +
                    "fresh water and consistent forage. Contact a vet only if symptoms worsen.",
                sourceCitation = "General veterinary guidance (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "horse_colic_01",
                species = "horse",
                condition = "Colic",
                symptoms = listOf(
                    "pawing at ground",
                    "rolling",
                    "looking at flank",
                    "absence of gut sounds",
                    "sweating"
                ),
                urgency = "Emergency",
                actionText = "Contact a veterinarian immediately. Remove feed, keep the horse " +
                    "calm, and walk it gently if it is safe to do so. Do not administer " +
                    "medication without veterinary guidance.",
                sourceCitation = "Merck Veterinary Manual - Colic in Horses (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "horse_choke_01",
                species = "horse",
                condition = "Choke (Esophageal Obstruction)",
                symptoms = listOf(
                    "coughing",
                    "nasal discharge with feed material",
                    "distress",
                    "neck extension"
                ),
                urgency = "Emergency",
                actionText = "Remove all feed and water immediately and call a veterinarian. Do " +
                    "not attempt to dislodge the obstruction yourself.",
                sourceCitation = "Merck Veterinary Manual - Esophageal Obstruction (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "horse_laminitis_01",
                species = "horse",
                condition = "Laminitis",
                symptoms = listOf(
                    "reluctance to move",
                    "heat in hooves",
                    "shifting weight between feet",
                    "lying down more than usual"
                ),
                urgency = "Monitor",
                actionText = "Restrict movement, provide soft bedding, and contact a " +
                    "veterinarian within 24 hours. Remove access to rich pasture.",
                sourceCitation = "Merck Veterinary Manual - Laminitis in Horses (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "horse_gascolic_01",
                species = "horse",
                condition = "Mild Gas Colic",
                symptoms = listOf(
                    "occasional pawing",
                    "mild discomfort",
                    "normal gut sounds",
                    "improves with walking"
                ),
                urgency = "Monitor",
                actionText = "Walk the horse gently and monitor closely for 30-60 minutes. " +
                    "Contact a veterinarian if symptoms worsen or gut sounds disappear.",
                sourceCitation = "General veterinary guidance (generalized, illustrative)"
            ),
            KnowledgeEntry(
                id = "horse_rainscald_01",
                species = "horse",
                condition = "Rain Scald (Dermatophilosis)",
                symptoms = listOf(
                    "scabby skin lesions",
                    "matted tufts of hair",
                    "mild discomfort when touched",
                    "no fever"
                ),
                urgency = "Non-urgent",
                actionText = "Keep the affected area dry and clean. Mild cases often resolve " +
                    "without treatment; consult a vet if lesions spread significantly.",
                sourceCitation = "General veterinary guidance (generalized, illustrative)"
            )
        )
    }
}
