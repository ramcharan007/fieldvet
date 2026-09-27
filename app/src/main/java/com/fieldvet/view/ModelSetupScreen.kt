package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fieldvet.view.theme.BorderLight
import com.fieldvet.view.theme.UrgencyEmergency
import com.fieldvet.viewmodel.ModelDownloadUiState
import kotlin.math.roundToInt

@Composable
fun ModelSetupScreen(
    uiState: ModelDownloadUiState,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Setting up FieldVet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = "Downloading the on-device AI model - a one-time step of about 660 MB.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val percent = uiState.downloadPercentOrNull()
                ProgressTrack(fraction = percent / 100f)

                when (uiState) {
                    is ModelDownloadUiState.Checking ->
                        Text("Preparing...", style = MaterialTheme.typography.titleMedium)

                    is ModelDownloadUiState.Downloading ->
                        Text("Downloading... $percent%", style = MaterialTheme.typography.titleMedium)

                    is ModelDownloadUiState.Verifying ->
                        Text("Verifying download...", style = MaterialTheme.typography.titleMedium)

                    is ModelDownloadUiState.Ready ->
                        Text("Ready.", style = MaterialTheme.typography.titleMedium)

                    is ModelDownloadUiState.Error -> {
                        Text(
                            "Download failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = UrgencyEmergency,
                        )
                        Text(uiState.message, style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        if (uiState is ModelDownloadUiState.Ready) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Continue")
            }
        } else {
            Text(
                text = "Keep the app open until setup finishes. This only happens once.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun ProgressTrack(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(BorderLight, RoundedCornerShape(4.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
        )
    }
}

private fun ModelDownloadUiState.downloadPercentOrNull(): Int = when (this) {
    is ModelDownloadUiState.Downloading ->
        if (totalBytes > 0) ((bytesDownloaded * 100.0) / totalBytes).roundToInt() else 0
    is ModelDownloadUiState.Verifying, is ModelDownloadUiState.Ready -> 100
    else -> 0
}
