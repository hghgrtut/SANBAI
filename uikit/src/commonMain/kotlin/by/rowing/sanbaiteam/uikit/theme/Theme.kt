package by.rowing.sanbaiteam.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
@Composable
fun SANBAITeamTheme(
    useLightStatusBars: Boolean? = null,
    useLightNavigationBars: Boolean? = null,
    shapes: Shapes = AppShapes,
    content: @Composable () -> Unit
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    PlatformSystemBars(
        useLightStatusBars = useLightStatusBars ?: !darkTheme,
        useLightNavigationBars = useLightNavigationBars ?: !darkTheme,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content
    )
}

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.S),
    small = RoundedCornerShape(Radius.S),
    medium = RoundedCornerShape(Radius.M),
    large = RoundedCornerShape(Radius.M),
    extraLarge = RoundedCornerShape(Radius._2XL),
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPalette.Blue50,
    onPrimary = ColorPalette.White,
    primaryContainer = ColorPalette.Blue95,
    onPrimaryContainer = ColorPalette.Blue20,
    inversePrimary = ColorPalette.Blue60,

    secondary = ColorPalette.Green50,
    onSecondary = ColorPalette.Grey10,
    secondaryContainer = ColorPalette.Green50,
    onSecondaryContainer = ColorPalette.Grey10,

    tertiary = ColorPalette.Orange50,
    onTertiary = ColorPalette.Grey10,
    tertiaryContainer = ColorPalette.Yellow50,
    onTertiaryContainer = ColorPalette.Grey10,

    background = ColorPalette.Grey95,
    onBackground = ColorPalette.Grey10,

    surface = ColorPalette.White,
    onSurface = ColorPalette.Grey10,
    surfaceVariant = ColorPalette.Grey90,
    onSurfaceVariant = ColorPalette.Grey40,
    surfaceTint = ColorPalette.Blue50,
    inverseSurface = ColorPalette.Grey20,
    inverseOnSurface = ColorPalette.Grey95,

    error = ColorPalette.Red50,
    onError = ColorPalette.White,
    errorContainer = ColorPalette.Red70,
    onErrorContainer = ColorPalette.Grey10,

    outline = ColorPalette.Grey60,
    outlineVariant = ColorPalette.Grey80,
    scrim = ColorPalette.Grey10,

    surfaceBright = ColorPalette.White,
    surfaceDim = ColorPalette.Grey90,
    surfaceContainerLowest = ColorPalette.White,
    surfaceContainerLow = ColorPalette.White,
    surfaceContainer = ColorPalette.White,
    surfaceContainerHigh = ColorPalette.White,
    surfaceContainerHighest = ColorPalette.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorPalette.Blue50,
    onPrimary = ColorPalette.White,
    primaryContainer = ColorPalette.Blue30,
    onPrimaryContainer = ColorPalette.Blue95,
    inversePrimary = ColorPalette.Blue60,

    secondary = ColorPalette.Green50,
    onSecondary = ColorPalette.Grey10,
    secondaryContainer = ColorPalette.Green50,
    onSecondaryContainer = ColorPalette.Grey10,

    tertiary = ColorPalette.Orange50,
    onTertiary = ColorPalette.Grey10,
    tertiaryContainer = ColorPalette.Orange50,
    onTertiaryContainer = ColorPalette.Grey10,

    background = ColorPalette.Grey10,
    onBackground = ColorPalette.Grey95,

    surface = ColorPalette.Grey20,
    onSurface = ColorPalette.Grey95,
    surfaceVariant = ColorPalette.Grey30,
    onSurfaceVariant = ColorPalette.Grey80,
    surfaceTint = ColorPalette.Blue50,
    inverseSurface = ColorPalette.Grey95,
    inverseOnSurface = ColorPalette.Grey10,

    error = ColorPalette.Red70,
    onError = ColorPalette.Grey10,
    errorContainer = ColorPalette.Red50,
    onErrorContainer = ColorPalette.White,

    outline = ColorPalette.Grey60,
    outlineVariant = ColorPalette.Grey40,
    scrim = ColorPalette.Grey10,

    surfaceBright = ColorPalette.Grey30,
    surfaceDim = ColorPalette.Grey10,
    surfaceContainerLowest = ColorPalette.Grey10,
    surfaceContainerLow = ColorPalette.Grey20,
    surfaceContainer = ColorPalette.Grey20,
    surfaceContainerHigh = ColorPalette.Grey30,
    surfaceContainerHighest = ColorPalette.Grey30,
)

@Composable
internal expect fun PlatformSystemBars(
    useLightStatusBars: Boolean,
    useLightNavigationBars: Boolean,
)
