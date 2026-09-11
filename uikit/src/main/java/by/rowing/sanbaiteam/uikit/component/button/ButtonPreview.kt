package by.rowing.sanbaiteam.uikit.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import by.rowing.sanbaiteam.uikit.R
import by.rowing.sanbaiteam.uikit.theme.Spacing
import com.checker.uikit3.modifier.ClickableState
import com.checker.uikit3.modifier.LocalClickableState

internal data class ButtonPreviewState(
    val backgroundColor: Color,
    val buttonColors: ButtonColors,
)

@Composable
internal fun ButtonPreviewContent(
    state: ButtonPreviewState,
    label: String = "Сообщить о нарушении",
) {
    CompositionLocalProvider(LocalClickableState provides ClickableState()) {
        Column(
            modifier = Modifier
                .background(state.backgroundColor)
                .padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.M),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                debounceClick = {},
                buttonColors = state.buttonColors,
                leadingIcon = { Icon(R.drawable.ic_check_16) },
                trailingIcon = { Icon(R.drawable.ic_close_16) },
                size = ButtonSize.LARGE,
            )
            ButtonSize.entries.forEach { size ->
                Button(
                    text = label,
                    debounceClick = {},
                    buttonColors = state.buttonColors,
                    leadingIcon = { Icon(R.drawable.ic_check_16) },
                    size = size,
                )
            }
            ButtonSize.entries.forEach { size ->
                Button(
                    text = label,
                    debounceClick = {},
                    enabled = false,
                    buttonColors = state.buttonColors,
                    leadingIcon = { Icon(R.drawable.ic_check_16) },
                    size = size,
                )
            }
            ButtonSize.entries.forEach { size ->
                Button(
                    text = label,
                    debounceClick = {},
                    loading = true,
                    buttonColors = state.buttonColors,
                    leadingIcon = { Icon(R.drawable.ic_check_16) },
                    size = size,
                )
            }
        }
    }
}

internal class PrimaryButtonPreviewProvider : PreviewParameterProvider<ButtonPreviewState> {
    override val values = sequenceOf(
        ButtonPreviewState(
            buttonColors = ButtonColors.default,
            backgroundColor = Color.White,
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.inverse,
            backgroundColor = Color.Black,
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.accent,
            backgroundColor = Color.White,
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.brand,
            backgroundColor = Color.White,
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.attention,
            backgroundColor = Color.White,
        ),
    )
}

internal class SecondaryButtonPreviewProvider : PreviewParameterProvider<ButtonPreviewState> {
    override val values = sequenceOf(
        ButtonPreviewState(
            buttonColors = ButtonColors.secondaryDefault,
            backgroundColor = Color.White
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.secondaryInverse,
            backgroundColor = Color.Black
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.secondaryAccent,
            backgroundColor = Color.White
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.secondaryBrand,
            backgroundColor = Color.Black
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.secondaryAttention,
            backgroundColor = Color.White
        ),
    )
}

internal class TextButtonPreviewProvider : PreviewParameterProvider<ButtonPreviewState> {
    override val values = sequenceOf(
        ButtonPreviewState(
            buttonColors = ButtonColors.textDefault,
            backgroundColor = Color.White
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.textInverse,
            backgroundColor = Color.Black
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.textAccent,
            backgroundColor = Color.White
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.textBrand,
            backgroundColor = Color.Black
        ),
        ButtonPreviewState(
            buttonColors = ButtonColors.textAttention,
            backgroundColor = Color.White
        ),
    )
}
