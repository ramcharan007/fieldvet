package com.fieldvet.model

import android.content.Context
import android.os.StatFs
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val MODEL_DOWNLOAD_URL =
    "https://github.com/ramcharan007/fieldvet/releases/download/gemma3-1b-it-q4-v1/Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm"
private const val MODEL_SHA256 = "1325ae366d31950f137c9c357b9fa89448b176d76998180c08ceaca78bba98be"
private const val STORAGE_SAFETY_MARGIN_BYTES = 50L * 1024 * 1024

const val KEY_BYTES_DOWNLOADED = "bytesDownloaded"
const val KEY_TOTAL_BYTES = "totalBytes"
const val KEY_STAGE = "stage"
const val STAGE_DOWNLOADING = "downloading"
const val STAGE_VERIFYING = "verifying"
const val KEY_ERROR_MESSAGE = "errorMessage"

class ModelDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tempFile = ModelStorage.tempFile(applicationContext)
        val modelFile = ModelStorage.modelFile(applicationContext)

        return try {
            val totalBytes = fetchContentLength()

            val availableBytes = StatFs(ModelStorage.baseDir(applicationContext).path).availableBytes
            if (availableBytes < totalBytes + STORAGE_SAFETY_MARGIN_BYTES) {
                return Result.failure(errorData("Not enough storage. Need about ${totalBytes / (1024 * 1024)}MB free."))
            }

            download(tempFile, totalBytes)

            setProgress(workDataOf(KEY_STAGE to STAGE_VERIFYING))
            val actualHash = sha256Of(tempFile)
            if (!actualHash.equals(MODEL_SHA256, ignoreCase = true)) {
                tempFile.delete()
                return Result.failure(errorData("Downloaded file failed verification. Please retry."))
            }

            tempFile.renameTo(modelFile)
            ModelStorage.markVerified(applicationContext, modelFile.length())
            Result.success()
        } catch (e: IOException) {
            tempFile.delete()
            Result.failure(errorData("Download failed: ${e.message ?: "network error"}. Please retry."))
        }
    }

    private fun errorData(message: String): Data = workDataOf(KEY_ERROR_MESSAGE to message)

    private fun fetchContentLength(): Long {
        val connection = URL(MODEL_DOWNLOAD_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.instanceFollowRedirects = true
        try {
            return connection.contentLengthLong
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun download(destination: File, totalBytes: Long) {
        val connection = URL(MODEL_DOWNLOAD_URL).openConnection() as HttpURLConnection
        try {
            connection.connect()
            var bytesDownloaded = 0L
            var lastReportedPercent = -1

            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            setProgress(
                                workDataOf(
                                    KEY_STAGE to STAGE_DOWNLOADING,
                                    KEY_BYTES_DOWNLOADED to bytesDownloaded,
                                    KEY_TOTAL_BYTES to totalBytes,
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
