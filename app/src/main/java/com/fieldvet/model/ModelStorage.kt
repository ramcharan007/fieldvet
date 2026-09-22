package com.fieldvet.model

import android.content.Context
import java.io.File
import androidx.core.content.edit

private const val MODEL_FILENAME = "model.litertlm"
private const val PREFS_NAME = "model_download"
private const val KEY_VERIFIED_SIZE = "verified_size"

object ModelStorage {

    fun modelFile(context: Context): File = File(baseDir(context), MODEL_FILENAME)

    fun tempFile(context: Context): File = File(baseDir(context), "$MODEL_FILENAME.tmp")

    fun markVerified(context: Context, fileSize: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putLong(KEY_VERIFIED_SIZE, fileSize)
            }
    }

    fun isVerified(context: Context): Boolean {
        val file = modelFile(context)
        if (!file.exists()) return false
        val verifiedSize = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_VERIFIED_SIZE, -1L)
        return verifiedSize == file.length()
    }

    fun baseDir(context: Context): File = context.getExternalFilesDir(null) ?: context.filesDir
}
