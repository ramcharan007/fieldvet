package com.fieldvet.view.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Background = Color(0xFFF7F5F0)
val BrandAccent = Color(0xFF2B5C4B)
val BrandAccentDark = Color(0xFF1F4438)
val BrandAccentTint = Color(0xFFE3ECE7)
val TextPrimary = Color(0xFF1A1A16)
val TextMuted = Color(0xFF6B6A62)
val BorderLight = Color(0xFFE4E1D6)
val BorderNeutral = Color(0xFFD8D4C8)

val UrgencyEmergency = Color(0xFFC4342B)
val UrgencyEmergencyTint = Color(0xFFF7E1DE)
val UrgencyUrgent = Color(0xFFC97A1E)
val UrgencyUrgentTint = Color(0xFFF5E7D2)
val UrgencyRoutine = Color(0xFF2F7A52)
val UrgencyRoutineTint = Color(0xFFDEEFE3)

val FieldVetColorScheme = lightColorScheme(
    background = Background,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Background,
    onSurfaceVariant = TextMuted,
    primary = BrandAccent,
    onPrimary = Color.White,
    primaryContainer = BrandAccentTint,
    onPrimaryContainer = BrandAccentDark,
    outline = BorderLight,
    outlineVariant = BorderNeutral,
    error = UrgencyEmergency,
    onError = Color.White,
    errorContainer = UrgencyEmergencyTint,
    onErrorContainer = UrgencyEmergency,
)

/**
 * Urgency is a domain concept (from KnowledgeEntry.urgency), not a Material role,
 * so these live as plain constants rather than inside the ColorScheme.
 */
object UrgencyColors {
    data class Palette(val accent: Color, val tint: Color)

    val Emergency = Palette(UrgencyEmergency, UrgencyEmergencyTint)
    val Urgent = Palette(UrgencyUrgent, UrgencyUrgentTint)
    val Routine = Palette(UrgencyRoutine, UrgencyRoutineTint)

    /** Maps KnowledgeEntry.urgency's raw values ("Emergency"/"Monitor"/"Non-urgent"). */
    fun forLevel(level: String?): Palette = when (level) {
        "Emergency" -> Emergency
        "Monitor" -> Urgent
        "Non-urgent" -> Routine
        else -> Palette(TextMuted, BorderLight)
    }
}
