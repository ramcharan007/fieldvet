package com.fieldvet.view.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FieldVetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FieldVetColorScheme, typography = FieldVetTypography) {
        Surface(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            color = MaterialTheme.colorScheme.background,
        ) {
            content()
        }
    }
}
