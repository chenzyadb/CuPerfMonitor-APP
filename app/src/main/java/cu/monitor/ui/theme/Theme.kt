package cu.monitor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFF888888),
    tertiary = Color(0xFF404040),
    background = Color(0xFF000000),
    surface = Color(0xFF202020)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    secondary = Color(0xFF888888),
    tertiary = Color(0xFFE0E0E0),
    background = Color(0xFFF8F8F8),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun CuPerfMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            if (darkTheme) {
                (view.context as Activity).window.statusBarColor = Color(0xFF000000).toArgb()
                (view.context as Activity).window.navigationBarColor = Color(0xFF000000).toArgb()
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = false
            } else {
                (view.context as Activity).window.statusBarColor = Color(0xFFF8F8F8).toArgb()
                (view.context as Activity).window.navigationBarColor = Color(0xFFF8F8F8).toArgb()
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}