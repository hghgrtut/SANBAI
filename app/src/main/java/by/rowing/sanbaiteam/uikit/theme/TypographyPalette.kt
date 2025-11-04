package by.rowing.sanbaiteam.uikit.theme

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.InterFontFamily
import kotlin.math.roundToInt

/**
 * figma: https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=2-11&p=f&t=Cwyq7FbrKyH7r243-0
 */
object TypographyPalette {

    /**
     * fontSize = 24.fontDp,
     * lineHeight = 28.fontDp,
     * fontWeight = FontWeight(600),
     */
    val H1
        @Composable get() = TextStyle(
            fontSize = 24.fontDp,
            lineHeight = 28.fontDp,
            fontWeight = FontWeight(600),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 20.fontDp,
     * lineHeight = 24.fontDp,
     * fontWeight = FontWeight(600),
     */
    val H2
        @Composable get() = TextStyle(
            fontSize = 20.fontDp,
            lineHeight = 24.fontDp,
            fontWeight = FontWeight(600),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 18.fontDp,
     * lineHeight = 24.fontDp,
     * fontWeight = FontWeight(600),
     */
    val H3
        @Composable get() = TextStyle(
            fontSize = 18.fontDp,
            lineHeight = 24.fontDp,
            fontWeight = FontWeight(600),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 16.fontDp,
     * lineHeight = 24.fontDp,
     * fontWeight = FontWeight(600),
     */
    val H4
        @Composable get() = TextStyle(
            fontSize = 16.fontDp,
            lineHeight = 24.fontDp,
            fontWeight = FontWeight(600),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 14.fontDp,
     * lineHeight = 20.fontDp,
     * fontWeight = FontWeight(600),
     */
    val H5
        @Composable get() = TextStyle(
            fontSize = 14.fontDp,
            lineHeight = 20.fontDp,
            fontWeight = FontWeight(600),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 16.fontDp,
     * lineHeight = 24.fontDp,
     * fontWeight = FontWeight(400),
     */
    val Body1Regular
        @Composable get() = TextStyle(
            fontSize = 16.fontDp,
            lineHeight = 24.fontDp,
            fontWeight = FontWeight(400),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 16.fontDp,
     * lineHeight = 24.fontDp,
     * fontWeight = FontWeight(500),
     */
    val Body1Medium
        @Composable get() = TextStyle(
            fontSize = 16.fontDp,
            lineHeight = 24.fontDp,
            fontWeight = FontWeight(500),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 14.fontDp,
     * lineHeight = 20.fontDp,
     * fontWeight = FontWeight(400),
     */
    val Body2Regular
        @Composable get() = TextStyle(
            fontSize = 14.fontDp,
            lineHeight = 20.fontDp,
            fontWeight = FontWeight(400),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 14.fontDp,
     * lineHeight = 20.fontDp,
     * fontWeight = FontWeight(500),
     */
    val Body2Medium
        @Composable get() = TextStyle(
            fontSize = 14.fontDp,
            lineHeight = 20.fontDp,
            fontWeight = FontWeight(500),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 12.fontDp,
     * lineHeight = 16.fontDp,
     * fontWeight = FontWeight(400)
     */
    val Body3Regular
        @Composable get() = TextStyle(
            fontSize = 12.fontDp,
            lineHeight = 16.fontDp,
            fontWeight = FontWeight(400),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 12.fontDp,
     * lineHeight = 16.fontDp,
     * fontWeight = FontWeight(500),
     */
    val Body3Medium
        @Composable get() = TextStyle(
            fontSize = 12.fontDp,
            lineHeight = 16.fontDp,
            fontWeight = FontWeight(500),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 11.fontDp,
     * lineHeight = 13.fontDp,
     * fontWeight = FontWeight(400)
     */
    val Caption1Regular
        @Composable get() = TextStyle(
            fontSize = 11.fontDp,
            lineHeight = 13.fontDp,
            fontWeight = FontWeight(400),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 11.fontDp,
     * lineHeight = 13.fontDp,
     * fontWeight = FontWeight(500),
     */
    val Caption1Medium
        @Composable get() = TextStyle(
            fontSize = 11.fontDp,
            lineHeight = 13.fontDp,
            fontWeight = FontWeight(500),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    /**
     * fontSize = 10.fontDp,
     * lineHeight = 12.fontDp,
     * fontWeight = FontWeight(500),
     */
    val Caption2
        @Composable get() = TextStyle(
            fontSize = 10.fontDp,
            lineHeight = 12.fontDp,
            fontWeight = FontWeight(500),
            fontFamily = InterFontFamily,
            lineHeightStyle = VerticalLineHeightStyle,
        )

    private val VerticalLineHeightStyle: LineHeightStyle
        @Composable get() = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        )

    private val Int.fontDp: TextUnit
        @Composable
        get() = with(LocalDensity.current) { dp().toSp() }

    fun Int.dp() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()
}

@Preview
@Composable
private fun Preview() {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut " +
            "labore et dolore magna aliqua"
    val styles = listOf(
        "H1" to TypographyPalette.H1,
        "H2" to TypographyPalette.H2,
        "H3" to TypographyPalette.H3,
        "H4" to TypographyPalette.H4,
        "H5" to TypographyPalette.H5,
        "Body1Regular" to TypographyPalette.Body1Regular,
        "Body1Medium" to TypographyPalette.Body1Medium,
        "Body2Regular" to TypographyPalette.Body2Regular,
        "Body2Medium" to TypographyPalette.Body2Medium,
        "Body3Regular" to TypographyPalette.Body3Regular,
        "Body3Medium" to TypographyPalette.Body3Medium,
        "Caption1Regular" to TypographyPalette.Caption1Regular,
        "Caption1Medium" to TypographyPalette.Caption1Medium,
        "Caption2" to TypographyPalette.Caption2,
    )

    Column {
        styles.forEach { (name, style) ->
            Row(
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .padding(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(3f),
                    text = name,
                    style = style
                )
                Text(
                    modifier = Modifier.weight(7f),
                    text = lorem,
                    style = style,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}