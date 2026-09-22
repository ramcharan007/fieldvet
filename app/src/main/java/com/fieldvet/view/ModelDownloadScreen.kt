package com.fieldvet.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fieldvet.viewmodel.ModelDownloadUiState
import kotlin.math.roundToInt

@Composable
fun ModelDownloadScreen(uiState: ModelDownloadUiState, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (uiState) {
                is ModelDownloadUiState.Checking -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Preparing...")
                }

                is ModelDownloadUiState.Downloading -> {
                    val percent = if (uiState.totalBytes > 0) {
                        (uiState.bytesDownloaded * 100.0 / uiState.totalBytes).roundToInt()
                    } else {
                        0
                    }
                    Text("Downloading model...", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text("$percent% - ${uiState.bytesDownloaded.toMb()} / ${uiState.totalBytes.toMb()} MB")
                }

                is ModelDownloadUiState.Verifying -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Verifying download...")
                }

                is ModelDownloadUiState.Ready -> Unit // not routed here; nothing to show

                is ModelDownloadUiState.Error -> {
                    Text("Download failed", style = MaterialTheme.typography.titleMedium)
                    Text(uiState.message)
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

private fun Long.toMb(): Long = this / (1024 * 1024)
