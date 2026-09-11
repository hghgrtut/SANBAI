package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.theme.ColorPalette

/**
 * Компонент Throbber
 *
 * Макет: [figma](https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=1516-3729)
 */
@Composable
fun Throbber(
    modifier: Modifier = Modifier,
    style: Throbber.Style = Throbber.Style.GREY,
) {
    CircularProgressIndicator(
        modifier = Modifier
            .requiredSize(24.dp)
            .then(modifier),
        color = style.color,
        trackColor = style.backgroundColor,
        strokeWidth = 2.5.dp,
        strokeCap = StrokeCap.Round,
    )
}

object Throbber {

    enum class Style(
        val color: Color,
        val backgroundColor: Color,
    ) {
        GREY(
            color = ColorPalette.Grey40,
            backgroundColor = ColorPalette.Grey80,
        ),
        YELLOW(
            color = ColorPalette.Yellow50New,
            backgroundColor = ColorPalette.Grey80,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Throbber()
}