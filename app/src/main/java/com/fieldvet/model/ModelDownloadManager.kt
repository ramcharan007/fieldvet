package com.fieldvet.model

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val UNIQUE_WORK_NAME = "model_download"

class ModelDownloadManager(private val context: Context) {

    suspend fun isModelReady(): Boolean = withContext(Dispatchers.IO) {
        ModelStorage.isVerified(context)
    }

    fun enqueueDownload() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ModelDownloadWorker>().build(),
        )
    }

    fun retry() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ModelDownloadWorker>().build(),
        )
    }

    fun observeWork(): Flow<WorkInfo?> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .map { it.firstOrNull() }
}
