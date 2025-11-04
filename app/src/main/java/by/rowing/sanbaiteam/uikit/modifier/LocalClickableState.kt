package com.checker.uikit3.modifier

import androidx.compose.runtime.compositionLocalOf

val LocalClickableState = compositionLocalOf<ClickableState> {
    error("No ClickableState provided")
}