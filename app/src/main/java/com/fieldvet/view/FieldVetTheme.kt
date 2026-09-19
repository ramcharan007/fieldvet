package com.fieldvet.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val FieldVetPurple = Color(0xFF6750A4)
private val FieldVetPurpleContainer = Color(0xFFEADDFF)

private val FieldVetColorScheme = lightColorScheme(
    primary = FieldVetPurple,
    onPrimary = Color.White,
    primaryContainer = FieldVetPurpleContainer,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

// Urgency is a domain concept (from KnowledgeEntry.urgency), not a Material role,
// so these live as plain constants rather than inside the ColorScheme.
object UrgencyColors {
    val Emergency = Color(0xFFF2B8B5)
    val Monitor = Color(0xFFFFD8A8)
    val NonUrgent = Color(0xFFB9EAB9)
}

@Composable
fun FieldVetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FieldVetColorScheme) {
        // MaterialTheme only sets the color palette in scope - it doesn't paint anything.
        // Without this Surface, the app window keeps showing the leftover dark XML theme's
        // background behind our content instead of FieldVetColorScheme.background.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
