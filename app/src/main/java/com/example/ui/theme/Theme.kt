package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CampusDarkColorScheme = darkColorScheme(
    primary = CampusIndigoPrimaryDark,
    onPrimary = CampusIndigoOnPrimaryDark,
    primaryContainer = CampusIndigoContainerDark,
    onPrimaryContainer = CampusIndigoOnContainerDark,
    secondary = CampusAmberSecondaryDark,
    onSecondary = CampusAmberOnSecondaryDark,
    secondaryContainer = CampusAmberContainerDark,
    onSecondaryContainer = CampusAmberOnContainerDark,
    background = CampusSurfaceDark,
    onBackground = CampusOnSurfaceDark,
    surface = CampusSurfaceDark,
    onSurface = CampusOnSurfaceDark,
    surfaceVariant = CampusSurfaceVariantDark,
    onSurfaceVariant = CampusOnSurfaceVariantDark,
    outline = CampusOutlineDark
)

private val CampusLightColorScheme = lightColorScheme(
    primary = CampusIndigoPrimary,
    onPrimary = CampusIndigoOnPrimary,
    primaryContainer = CampusIndigoContainer,
    onPrimaryContainer = CampusIndigoOnContainer,
    secondary = CampusAmberSecondary,
    onSecondary = CampusAmberOnSecondary,
    secondaryContainer = CampusAmberContainer,
    onSecondaryContainer = CampusAmberOnContainer,
    background = CampusSurfaceLight,
    onBackground = CampusOnSurfaceLight,
    surface = CampusSurfaceLight,
    onSurface = CampusOnSurfaceLight,
    surfaceVariant = CampusSurfaceVariantLight,
    onSurfaceVariant = CampusOnSurfaceVariantLight,
    outline = CampusOutlineLight
)

@Composable
fun CampusDirectoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to showcase deliberate campus indigo & amber branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CampusDarkColorScheme
        else -> CampusLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CampusDirectoryTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
