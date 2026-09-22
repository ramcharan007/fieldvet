package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fieldvet.model.TriageResult
import com.fieldvet.viewmodel.TriageUiState

// Gemma tends to answer with lightweight markdown (bold, "- "/"* " bullets, "1. "
// numbered steps) even though nothing in the prompt asks for it. Rendered as plain
// text that shows up as literal asterisks, so this does just enough parsing to
// turn it into readable Compose text without pulling in a full markdown library.
private val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")
private val BULLET_LINE_REGEX = Regex("""^[-*•]\s+(.*)$""")
private val NUMBERED_LINE_REGEX = Regex("""^(\d+)[.)]\s+(.*)$""")

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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Triage report",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        if (result.isSuccess) {
            UrgencyBanner(result.urgencyLevel)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                FormattedResponseText(
                    text = result.responseText.orEmpty(),
                    modifier = Modifier.padding(16.dp),
                )
            }

            result.sourceCitation?.let {
                Text(
                    text = "Source: $it",
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
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
            )
        }

        Button(onClick = onNewTriage, modifier = Modifier.fillMaxWidth()) {
            Text("Try again")
        }
    }
}

@Composable
private fun UrgencyBanner(urgencyLevel: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = urgencyColor(urgencyLevel)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(urgencyAccentColor(urgencyLevel), CircleShape),
            )
            Text(
                text = "${urgencyLevel ?: "Unknown"} level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun urgencyColor(level: String?): Color = when (level) {
    "Emergency" -> UrgencyColors.Emergency
    "Monitor" -> UrgencyColors.Monitor
    "Non-urgent" -> UrgencyColors.NonUrgent
    else -> Color.LightGray
}

private fun urgencyAccentColor(level: String?): Color = when (level) {
    "Emergency" -> Color(0xFFB3261E)
    "Monitor" -> Color(0xFFB86A00)
    "Non-urgent" -> Color(0xFF2E7D32)
    else -> Color.DarkGray
}

@Composable
private fun FormattedResponseText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        text.trim().lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            val bulletMatch = BULLET_LINE_REGEX.matchEntire(line)
            val numberedMatch = NUMBERED_LINE_REGEX.matchEntire(line)
            val annotated = when {
                bulletMatch != null -> listItemAnnotatedString("•  ", bulletMatch.groupValues[1])
                numberedMatch != null ->
                    listItemAnnotatedString("${numberedMatch.groupValues[1]}.  ", numberedMatch.groupValues[2])
                else -> buildAnnotatedString { appendWithBold(line) }
            }
            Text(text = annotated, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun listItemAnnotatedString(prefix: String, body: String): AnnotatedString = buildAnnotatedString {
    withStyle(ParagraphStyle(textIndent = TextIndent(restLine = 22.sp))) {
        append(prefix)
        appendWithBold(body)
    }
}

private fun AnnotatedString.Builder.appendWithBold(text: String) {
    var lastIndex = 0
    for (match in BOLD_REGEX.findAll(text)) {
        append(text.substring(lastIndex, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}
