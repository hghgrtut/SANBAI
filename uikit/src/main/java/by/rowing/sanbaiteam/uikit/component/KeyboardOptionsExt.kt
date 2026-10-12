package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
fun KeyboardOptions.Companion.time(imeAction: ImeAction = ImeAction.Next) = KeyboardOptions(
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Ascii,
    imeAction = imeAction
)

@AllowDetektPublic
fun KeyboardOptions.Companion.decimal(imeAction: ImeAction = ImeAction.Next) = KeyboardOptions(
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Decimal,
    imeAction = imeAction
)

@AllowDetektPublic
fun KeyboardOptions.Companion.number(imeAction: ImeAction = ImeAction.Next) = KeyboardOptions(
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Number,
    imeAction = imeAction
)