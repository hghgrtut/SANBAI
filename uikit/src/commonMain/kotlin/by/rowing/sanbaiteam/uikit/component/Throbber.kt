package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Компонент Throbber
 *
 * Макет: [figma](https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=1516-3729)
 */
@AllowDetektPublic
@Composable
fun Throbber(
    modifier: Modifier = Modifier,
    style: Throbber.Style = Throbber.Style.GREY,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedColor = if (color == Color.Unspecified) {
        when (style) {
            Throbber.Style.GREY -> colorScheme.onSurfaceVariant
            Throbber.Style.YELLOW -> colorScheme.tertiary
        }
    } else {
        color
    }
    val resolvedTrackColor = if (trackColor == Color.Unspecified) {
        colorScheme.surfaceVariant
    } else {
        trackColor
    }
    CircularProgressIndicator(
        modifier = Modifier
            .requiredSize(24.dp)
            .then(modifier),
        color = resolvedColor,
        trackColor = resolvedTrackColor,
        strokeWidth = 2.5.dp,
        strokeCap = StrokeCap.Round,
    )
}

@AllowDetektPublic
object Throbber {

    enum class Style {
        GREY,
        YELLOW,
    }
}

@Preview
@Composable
private fun Preview() {
    Throbber()
}
