package com.labodescms.santetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val SanteDarkColorScheme = darkColorScheme(
    background = AppColors.Background,
    surface = AppColors.Card,
    surfaceVariant = AppColors.Sheet,
    primary = AppColors.Gold,
    onPrimary = AppColors.GoldOnGold,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    outline = AppColors.Border,
)

private val SanteTypography = Typography(
    bodyLarge = AppType.Body,
    bodyMedium = AppType.Body.copy(fontWeight = FontWeight.Medium),
    titleLarge = AppType.ScreenTitle,
    labelLarge = AppType.PillButton,
)

@Composable
fun SanteTrackerTheme(
    content: @Composable () -> Unit,
) {
    // The app is always dark-themed by design; system theme is ignored intentionally.
    MaterialTheme(
        colorScheme = SanteDarkColorScheme,
        typography = SanteTypography,
        content = content,
    )
}
