package by.rowing.sanbaiteam.uikit

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import by.rowing.sanbaiteam.R

val InterFontFamily = FontFamily(
    Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
    Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
    Font(resId = R.font.inter_semi_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.inter_bold, weight = FontWeight.Bold),
    Font(resId = R.font.inter_italic, style = FontStyle.Italic),
    Font(resId = R.font.inter_medium_italic, style = FontStyle.Italic, weight = FontWeight.Medium),
    Font(resId = R.font.inter_semi_bold_italic, style = FontStyle.Italic, weight = FontWeight.SemiBold),
    Font(resId = R.font.inter_bold_italic, style = FontStyle.Italic, weight = FontWeight.Bold),
)