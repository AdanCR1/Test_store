package com.example.test_store.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.test_store.ui.theme.PrimaryBlue
import com.example.test_store.ui.theme.PrimaryDarkBlue
import com.example.test_store.ui.theme.SecondaryGray
import com.example.test_store.ui.theme.SecondaryDarkGray
import com.example.test_store.ui.theme.TertiaryGreen
import com.example.test_store.ui.theme.TertiaryDarkGreen
import com.example.test_store.ui.theme.BackgroundLight
import com.example.test_store.ui.theme.SurfaceWhite
import com.example.test_store.ui.theme.TextDark
import com.example.test_store.ui.theme.TextLight
import com.example.test_store.ui.theme.ErrorRed

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkBlue,
    onPrimary = TextLight,
    secondary = SecondaryDarkGray,
    onSecondary = TextLight,
    tertiary = TertiaryDarkGreen,
    onTertiary = TextLight,
    background = TextDark,
    onBackground = TextLight,
    surface = TextDark,
    onSurface = TextLight,
    error = ErrorRed,
    onError = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextLight,
    secondary = SecondaryGray,
    onSecondary = TextLight,
    tertiary = TertiaryGreen,
    onTertiary = TextLight,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    error = ErrorRed,
    onError = TextLight
)

@Composable
fun Test_storeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}