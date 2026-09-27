package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.fieldvet.view.theme.UrgencyColors

private val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")
private val BULLET_LINE_REGEX = Regex("""^[-*•]\s+(.*)$""")
private val NUMBERED_LINE_REGEX = Regex("""^(\d+)[.)]\s+(.*)$""")

@Composable
fun TriageResponseScreen(result: TriageResult, onNewSymptomCheck: () -> Unit) {
    val palette = UrgencyColors.forLevel(result.urgencyLevel)
    val isUrgent = result.urgencyLevel == "Emergency" || result.urgencyLevel == "Monitor"

    Column(modifier = Modifier.fillMaxSize()) {
        UrgencyBanner(urgencyLevel = result.urgencyLevel, accent = palette.accent)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "What to do",
                style = MaterialTheme.typography.titleLarge,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                FormattedResponseText(
                    text = result.responseText.orEmpty(),
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (isUrgent) {
                VetContactBanner(accent = palette.accent, tint = palette.tint)
            }

            result.sourceCitation?.let {
                Text(
                    text = "Source: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedButton(
                onClick = onNewSymptomCheck,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("New Symptom Check")
            }
        }
    }
}

@Composable
private fun UrgencyBanner(urgencyLevel: String?, accent: Color) {
    val (label, icon) = when (urgencyLevel) {
        "Emergency" -> "Emergency — act now" to "⚠"
        "Monitor" -> "Urgent — see a vet soon" to "⚠"
        else -> "Routine" to "✓"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = icon, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun VetContactBanner(accent: Color, tint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(tint, RoundedCornerShape(8.dp)),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Text(
            text = "Contact your local vet as soon as possible.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp),
        )
    }
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
