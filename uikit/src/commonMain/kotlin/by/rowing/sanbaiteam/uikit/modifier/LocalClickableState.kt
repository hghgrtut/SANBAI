package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.runtime.compositionLocalOf
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
val LocalClickableState = compositionLocalOf<ClickableState> {
    error("No ClickableState provided")
}