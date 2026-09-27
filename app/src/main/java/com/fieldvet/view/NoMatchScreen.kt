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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fieldvet.view.theme.BorderLight
import com.fieldvet.view.theme.BorderNeutral
import com.fieldvet.view.theme.TextMuted

@Composable
fun NoMatchScreen(onNewSymptomCheck: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 40.dp)
                .size(64.dp)
                .background(BorderLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", style = MaterialTheme.typography.headlineSmall, color = TextMuted)
        }

        Text(
            text = "No confident match found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "We couldn't match these symptoms to anything in the knowledge base " +
                "confidently enough to give guidance. This isn't a failure - it means the " +
                "safest step is a vet's judgement.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(IntrinsicSize.Min)
                .background(BorderLight, RoundedCornerShape(8.dp)),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(BorderNeutral),
            )
            Text(
                text = "Please contact a vet directly for guidance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onNewSymptomCheck,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("New Symptom Check")
        }
    }
}
