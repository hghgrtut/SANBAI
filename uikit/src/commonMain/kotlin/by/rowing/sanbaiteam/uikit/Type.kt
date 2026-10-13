package by.rowing.sanbaiteam.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import by.rowing.sanbaiteam.uikit.resources.Res
import by.rowing.sanbaiteam.uikit.resources.inter_bold
import by.rowing.sanbaiteam.uikit.resources.inter_bold_italic
import by.rowing.sanbaiteam.uikit.resources.inter_italic
import by.rowing.sanbaiteam.uikit.resources.inter_medium
import by.rowing.sanbaiteam.uikit.resources.inter_medium_italic
import by.rowing.sanbaiteam.uikit.resources.inter_regular
import by.rowing.sanbaiteam.uikit.resources.inter_semi_bold
import by.rowing.sanbaiteam.uikit.resources.inter_semi_bold_italic
import org.jetbrains.compose.resources.Font

internal val InterFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(
            resource = Res.font.inter_regular,
            weight = FontWeight.Normal
        ),
        Font(
            resource = Res.font.inter_medium,
            weight = FontWeight.Medium
        ),
        Font(
            resource = Res.font.inter_semi_bold,
            weight = FontWeight.SemiBold
        ),
        Font(
            resource = Res.font.inter_bold,
            weight = FontWeight.Bold
        ),
        Font(
            resource = Res.font.inter_italic,
            style = FontStyle.Italic
        ),
        Font(
            resource = Res.font.inter_medium_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Medium
        ),
        Font(
            resource = Res.font.inter_semi_bold_italic,
            style = FontStyle.Italic,
            weight = FontWeight.SemiBold
        ),
        Font(
            resource = Res.font.inter_bold_italic,
            style = FontStyle.Italic,
            weight = FontWeight.Bold
        ),
    )
