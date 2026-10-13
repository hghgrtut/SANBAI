package by.rowing.sanbaiteam.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
@Composable
fun SANBAITeamTheme(
    useLightStatusBars: Boolean = true,
    useLightNavigationBars: Boolean = true,
    shapes: Shapes = MaterialTheme.shapes,
    content: @Composable () -> Unit
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = ColorPalette.Blue50,
            onPrimary = ColorPalette.White,
            primaryContainer = ColorPalette.Blue30,
            onPrimaryContainer = ColorPalette.White,
            secondary = ColorPalette.Green50,
            onSecondary = ColorPalette.White,
            background = ColorPalette.Grey10,
            onBackground = ColorPalette.Grey95,
            surface = ColorPalette.Grey20,
            onSurface = ColorPalette.Grey95,
        )
    } else {
        lightColorScheme(
            primary = ColorPalette.Blue50,
            onPrimary = ColorPalette.White,
            primaryContainer = ColorPalette.Blue95,
            onPrimaryContainer = ColorPalette.Blue20,
            secondary = ColorPalette.Green50,
            onSecondary = ColorPalette.White,
            background = ColorPalette.Grey95,
            onBackground = ColorPalette.Grey10,
            surface = ColorPalette.White,
            onSurface = ColorPalette.Grey10,
            surfaceVariant = ColorPalette.Grey90,
            onSurfaceVariant = ColorPalette.Grey40,
        )
    }
    PlatformSystemBars(
        useLightStatusBars = useLightStatusBars,
        useLightNavigationBars = useLightNavigationBars,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content
    )
}

@Composable
internal expect fun PlatformSystemBars(
    useLightStatusBars: Boolean,
    useLightNavigationBars: Boolean,
)
