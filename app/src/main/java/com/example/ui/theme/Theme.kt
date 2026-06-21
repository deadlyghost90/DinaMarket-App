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

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryBlue,
    background = LightBackground,
    surface = SurfaceWhite,
    onPrimary = AccentWhite,
    onSecondary = AccentWhite,
    onBackground = DeepCharcoal,
    onSurface = DeepCharcoal,
    outline = BorderGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force Light Mode only
  dynamicColor: Boolean = false, // Force custom style design
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
