package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fieldvet.model.TriageResult
import com.fieldvet.viewmodel.TriageUiState

@Composable
fun ResponseScreen(
    uiState: TriageUiState,
    onNewTriage: () -> Unit,
) {
    when (uiState) {
        is TriageUiState.Success -> ResponseContent(uiState.result, onNewTriage)
        is TriageUiState.Error -> ErrorContent(uiState.message, onNewTriage)
        TriageUiState.Idle, TriageUiState.Loading -> Unit // not routed here; nothing to show
    }
}

@Composable
private fun ResponseContent(result: TriageResult, onNewTriage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Report", style = MaterialTheme.typography.headlineSmall)

        if (result.isSuccess) {
            UrgencyBanner(result.urgencyLevel)
            Text(result.responseText.orEmpty())
            result.sourceCitation?.let {
                Text(
                    "Source: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(result.errorMessage.orEmpty())
        }

        Button(onClick = onNewTriage, modifier = Modifier.fillMaxWidth()) {
            Text("New triage")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onNewTriage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.headlineSmall)
        Text(message)
        Button(onClick = onNewTriage, modifier = Modifier.fillMaxWidth()) {
            Text("Try again")
        }
    }
}

@Composable
private fun UrgencyBanner(urgencyLevel: String?) {
    Text(
        text = "$urgencyLevel Level",
        modifier = Modifier
            .fillMaxWidth()
            .background(urgencyColor(urgencyLevel), RoundedCornerShape(12.dp))
            .padding(12.dp),
        style = MaterialTheme.typography.titleMedium,
    )
}

private fun urgencyColor(level: String?): Color = when (level) {
    "Emergency" -> UrgencyColors.Emergency
    "Monitor" -> UrgencyColors.Monitor
    "Non-urgent" -> UrgencyColors.NonUrgent
    else -> Color.LightGray
}
