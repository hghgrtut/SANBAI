package by.rowing.sanbaiteam.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

/**
 * figma: https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=2-11&p=f&t=Cwyq7FbrKyH7r243-0
 */
object TypographyPaletteSp {

    /**
     * fontSize = 24.sp,
     * lineHeight = 28.sp,
     * fontWeight = FontWeight(600),
     */
    val H1
        @Composable get() = TypographyPalette.H1.copy(
            fontSize = 24.sp,
            lineHeight = 28.sp,
        )

    /**
     * fontSize = 20.sp,
     * lineHeight = 24.sp,
     * fontWeight = FontWeight(600),
     */
    val H2
        @Composable get() = TypographyPalette.H2.copy(
            fontSize = 20.sp,
            lineHeight = 24.sp,
        )

    /**
     * fontSize = 18.sp,
     * lineHeight = 24.sp,
     * fontWeight = FontWeight(600),
     */
    val H3
        @Composable get() = TypographyPalette.H3.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )

    /**
     * fontSize = 16.sp,
     * lineHeight = 24.sp,
     * fontWeight = FontWeight(600),
     */
    val H4
        @Composable get() = TypographyPalette.H4.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )

    /**
     * fontSize = 14.sp,
     * lineHeight = 20.sp,
     * fontWeight = FontWeight(600),
     */
    val H5
        @Composable get() = TypographyPalette.H5.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    /**
     * fontSize = 16.sp,
     * lineHeight = 24.sp,
     * fontWeight = FontWeight(400),
     */
    val Body1Regular
        @Composable get() = TypographyPalette.Body1Regular.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )

    /**
     * fontSize = 16.sp,
     * lineHeight = 24.sp,
     * fontWeight = FontWeight(500),
     */
    val Body1Medium
        @Composable get() = TypographyPalette.Body1Medium.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )

    /**
     * fontSize = 14.sp,
     * lineHeight = 20.sp,
     * fontWeight = FontWeight(400),
     */
    val Body2Regular
        @Composable get() = TypographyPalette.Body2Regular.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    /**
     * fontSize = 14.sp,
     * lineHeight = 20.sp,
     * fontWeight = FontWeight(500),
     */
    val Body2Medium
        @Composable get() = TypographyPalette.Body2Medium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    /**
     * fontSize = 12.sp,
     * lineHeight = 16.sp,
     * fontWeight = FontWeight(400)
     */
    val Body3Regular
        @Composable get() = TypographyPalette.Body3Regular.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    /**
     * fontSize = 12.sp,
     * lineHeight = 16.sp,
     * fontWeight = FontWeight(500),
     */
    val Body3Medium
        @Composable get() = TypographyPalette.Body3Medium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    /**
     * fontSize = 11.sp,
     * lineHeight = 13.sp,
     * fontWeight = FontWeight(400),
     */
    val Caption1Regular
        @Composable get() = TypographyPalette.Caption1Regular.copy(
            fontSize = 11.sp,
            lineHeight = 13.sp,
        )

    /**
     * fontSize = 11.sp,
     * lineHeight = 13.sp,
     * fontWeight = FontWeight(500),
     */
    val Caption1Medium
        @Composable get() = TypographyPalette.Caption1Medium.copy(
            fontSize = 11.sp,
            lineHeight = 13.sp,
        )

    /**
     * fontSize = 10.sp,
     * lineHeight = 12.sp,
     * fontWeight = FontWeight(500),
     */
    val Caption2
        @Composable get() = TypographyPalette.Caption2.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
        )
}