package com.fieldvet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fieldvet.view.theme.BorderLight
import com.fieldvet.view.theme.UrgencyEmergency
import com.fieldvet.view.theme.UrgencyEmergencyTint
import com.fieldvet.viewmodel.TriageUiState

// Chip labels are sent verbatim as FTS5 search terms, so each one is worded to contain
// words that appear in that species' entries in assets/knowledge_base.json.
private val SYMPTOM_CHIPS_BY_SPECIES = mapOf(
    "cattle" to listOf(
        "Bloat", "Lameness", "Can't stand", "Diarrhea",
        "Coughing", "Fever", "Swollen udder", "Straining",
    ),
    "horse" to listOf(
        "Pawing or rolling", "Lameness", "Cough", "Nasal discharge",
        "Diarrhea", "Fever", "Stiff muscles", "Wound",
    ),
)

@Composable
fun SymptomInputScreen(
    species: String,
    uiState: TriageUiState,
    onBack: () -> Unit,
    onSubmit: (query: String) -> Unit,
) {
    val isLoading = uiState is TriageUiState.Loading
    val symptomChips = SYMPTOM_CHIPS_BY_SPECIES[species.lowercase()].orEmpty()
    val selectedChips = remember { mutableStateListOf<String>() }
    var freeText by remember { mutableStateOf("") }

    fun submit() {
        val query = listOf(selectedChips.joinToString(", "), freeText.trim())
            .filter { it.isNotBlank() }
            .joinToString(". ")
        onSubmit(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, BorderLight, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("‹", style = MaterialTheme.typography.titleLarge)
            }
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = species,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Text(
            text = "What's wrong?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            text = "Tap all symptoms that apply, or describe below.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        symptomChips.chunked(2).forEach { rowChips ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowChips.forEach { chip ->
                    val selected = chip in selectedChips
                    SymptomChip(
                        label = chip,
                        selected = selected,
                        modifier = Modifier.weight(1f),
                        onToggle = {
                            if (selected) selectedChips.remove(chip) else selectedChips.add(chip)
                        },
                    )
                }
            }
        }

        Text(
            text = "Anything else?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        OutlinedTextField(
            value = freeText,
            onValueChange = { freeText = it },
            placeholder = { Text("e.g. stopped drinking water since yesterday") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
        )

        if (uiState is TriageUiState.Error) {
            InlineErrorBanner(message = uiState.message, onRetry = ::submit)
        }

        Button(
            onClick = ::submit,
            enabled = (selectedChips.isNotEmpty() || freeText.isNotBlank()) && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Get Advice")
            }
        }
    }
}

@Composable
private fun SymptomChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    Box(
        modifier = modifier
            .height(56.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else BorderLight,
                shape = RoundedCornerShape(12.dp),
            )
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InlineErrorBanner(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(UrgencyEmergencyTint, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = UrgencyEmergency,
        )
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Retry")
        }
    }
}
