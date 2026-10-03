package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fieldvet.view.theme.BorderLight
import com.fieldvet.view.theme.TextMuted

private data class SpeciesOption(
    val id: String,
    val label: String,
    val iconLetter: String,
    val enabled: Boolean,
)

private val SPECIES_OPTIONS = listOf(
    SpeciesOption(id = "Cattle", label = "Cattle", iconLetter = "C", enabled = true),
    SpeciesOption(id = "Horse", label = "Horse", iconLetter = "H", enabled = true),
    SpeciesOption(id = "Sheep", label = "Sheep", iconLetter = "S", enabled = false),
    SpeciesOption(id = "Goat", label = "Goat", iconLetter = "G", enabled = false),
)

@Composable
fun SpeciesSelectScreen(onSpeciesSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Which animal?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Select the animal you need to check.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SPECIES_OPTIONS.forEach { option ->
                SpeciesCard(option = option, onClick = { onSpeciesSelected(option.id) })
            }
        }
    }
}

@Composable
private fun SpeciesCard(option: SpeciesOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .alpha(if (option.enabled) 1f else 0.55f)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .then(
                if (option.enabled) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.iconLetter,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (option.enabled) "Tap to check symptoms" else "Coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        }

        if (option.enabled) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = TextMuted,
            )
        }
    }
}
