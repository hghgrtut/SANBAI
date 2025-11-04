package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import com.checker.uikit3.modifier.clearFocusOnKeyboardDismiss

/** https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=423-630 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: Boolean = false,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    colors: TextFieldColors = getColors(),
    contentPadding: PaddingValues = PaddingValues(DEFAULT_PADDING_VALUES.dp),
    textStyle: TextStyle = TEXT_STYLE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
): Unit = TextField(
    modifier = modifier,
    value = value,
    onValueChange = onValueChange,
    label = label?.let { AnnotatedString(it) },
    placeholder = placeholder?.let { AnnotatedString(it) },
    enabled = enabled,
    readOnly = readOnly,
    error = error,
    supportingText = supportingText,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    singleLine = singleLine,
    colors = colors,
    contentPadding = contentPadding,
    textStyle = textStyle,
    keyboardOptions = keyboardOptions,
    visualTransformation = visualTransformation
)

/** https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=423-630 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: AnnotatedString? = null,
    placeholder: AnnotatedString? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: Boolean = false,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    colors: TextFieldColors = getColors(),
    contentPadding: PaddingValues = PaddingValues(DEFAULT_PADDING_VALUES.dp),
    textStyle: TextStyle = TEXT_STYLE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var focused by remember { mutableStateOf(false) }
    val isSmallText = focused || value.isNotEmpty()

    val labelComposable: @Composable (() -> Unit)? = label?.let { annotatedString ->
        {
            TextFieldLabel(
                label = annotatedString,
                isSmallText = isSmallText
            )
        }
    }

    val placeholderComposable: @Composable (() -> Unit)? = placeholder?.let {
        {
            if (!focused) {
                TextFieldPlaceholder(
                    it
                )
            }
        }
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        enabled = enabled,
        readOnly = readOnly,
        error = error,
        singleLine = singleLine,
        label = labelComposable,
        placeholder = placeholderComposable,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors,
        contentPadding = contentPadding,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}

/** https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=423-630 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: Boolean = false,
    singleLine: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = getColors(),
    contentPadding: PaddingValues = PaddingValues(DEFAULT_PADDING_VALUES.dp),
    textStyle: TextStyle = TEXT_STYLE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        modifier = modifier
            .textFieldPadding(isLabelPassed = label != null)
            .clearFocusOnKeyboardDismiss(),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(colors.cursorColor),
        interactionSource = interactionSource,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            TextFieldDecorationBox(
                value = value,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = supportingText,
                enabled = enabled,
                colors = colors,
                error = error,
                singleLine = singleLine,
                interactionSource = interactionSource,
                contentPadding = contentPadding,
            )
        }
    )
}

/** https://www.figma.com/design/J0LnHWw2Q3FMDPeIf4Wk2A/UI-Kit-3-Mobile?node-id=423-630 */
@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String = "",
    error: Boolean = false,
    singleLine: Boolean = true,
    backgroundColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(DEFAULT_PADDING_VALUES.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = getColors().copy(
        focusedContainerColor = backgroundColor,
        unfocusedContainerColor = backgroundColor
    )
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        modifier = modifier.textFieldPadding(isLabelPassed = label != null),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = TEXT_STYLE,
        cursorBrush = SolidColor(colors.cursorColor),
        interactionSource = interactionSource,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            TextFieldDecorationBox(
                contentPadding = contentPadding,
                value = value.text,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = supportingText,
                enabled = enabled,
                colors = colors,
                error = error,
                singleLine = singleLine,
                interactionSource = interactionSource
            )
        }
    )
}

@Composable
fun getColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ColorPalette.White,
    unfocusedContainerColor = ColorPalette.White,
    errorContainerColor = ColorPalette.White,
    focusedSupportingTextColor = ColorPalette.Grey60,
    errorSupportingTextColor = ColorPalette.Red50,
    disabledSupportingTextColor = ColorPalette.Grey80,
    unfocusedLabelColor = ColorPalette.Grey40,
    focusedLabelColor = ColorPalette.Grey40,
    disabledLabelColor = ColorPalette.Grey80,
    errorLabelColor = ColorPalette.Grey40,
    unfocusedBorderColor = ColorPalette.Grey80,
    focusedBorderColor = ColorPalette.Grey20,
    errorBorderColor = ColorPalette.Red70,
)

@Composable
private fun TextFieldLabel(
    label: AnnotatedString,
    isSmallText: Boolean,
) {
    Text(
        text = label,
        style = if (isSmallText) TypographyPalette.Body3Regular else TypographyPalette.Body1Regular,
    )
}

@Composable
private fun TextFieldPlaceholder(label: AnnotatedString) {
    Text(
        text = label,
        style = TypographyPalette.Body1Regular,
        color = ColorPalette.Grey60,
    )
}

private fun Modifier.textFieldPadding(
    isLabelPassed: Boolean
): Modifier = if (isLabelPassed) {
    padding(top = BorderOutlinedTextFieldTopPadding)
} else {
    this
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TextFieldDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)?,
    label: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    supportingText: String?,
    enabled: Boolean,
    error: Boolean,
    singleLine: Boolean,
    colors: TextFieldColors,
    interactionSource: MutableInteractionSource,
    contentPadding: PaddingValues = PaddingValues(DEFAULT_PADDING_VALUES.dp),
) {
    OutlinedTextFieldDefaults.DecorationBox(
        contentPadding = contentPadding,
        value = value,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        enabled = enabled,
        isError = error,
        interactionSource = interactionSource,
        colors = colors,
        placeholder = placeholder,
        supportingText = supportingText?.let {
            {
                TextFieldSupportingText(
                    supportingText = supportingText,
                    enabled = enabled,
                    error = error,
                    colors = colors,
                )
            }
        },
        container = {
            TextFieldContainer(
                enabled = enabled,
                error = error,
                interactionSource = interactionSource,
                colors = colors
            )
        }
    )
}

@Composable
private fun TextFieldSupportingText(
    supportingText: String,
    enabled: Boolean,
    error: Boolean,
    colors: TextFieldColors,
) {
    val color = when {
        !enabled -> colors.disabledSupportingTextColor
        error -> colors.errorSupportingTextColor
        else -> colors.focusedSupportingTextColor
    }
    Text(
        text = supportingText,
        style = TypographyPalette.Body3Regular,
        color = color
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TextFieldContainer(
    enabled: Boolean,
    error: Boolean,
    interactionSource: MutableInteractionSource,
    colors: TextFieldColors
) {
    OutlinedTextFieldDefaults.Container(
        enabled = enabled,
        isError = error,
        interactionSource = interactionSource,
        colors = colors,
        shape = RoundedCornerShape(16.dp),
        focusedBorderThickness = 1.dp,
        unfocusedBorderThickness = 1.dp
    )
}

private val TEXT_STYLE
    @Composable
    get() = TypographyPalette.Body1Regular
private val BorderOutlinedTextFieldTopPadding = 8.dp

@Preview
@Composable
private fun TextFieldPreview() {
    ComposePreviewWrapper {

        val focusManager = LocalFocusManager.current

        @Composable
        fun Icon() {
            IconButton(onClick = { focusManager.clearFocus() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_16),
                    contentDescription = null,
                )
            }
        }

        Column {
            var supportingText by remember { mutableStateOf("Supporting Text example") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = supportingText,
                onValueChange = { supportingText = it },
                label = "Label for Supporting text",
                placeholder = "Supporting text placeholder",
                leadingIcon = { Icon() },
                trailingIcon = { Icon() },
            )

            var error by remember { mutableStateOf(false) }
            var leadingIcon by remember { mutableStateOf(false) }
            var trailingIcon by remember { mutableStateOf(false) }
            Row {
                Checkbox(
                    checked = if (error) CheckedState.CHECKED else CheckedState.UNCHECKED,
                    label = "Error?",
                    click = { error = !error }
                )

                Checkbox(
                    checked = if (leadingIcon) CheckedState.CHECKED else CheckedState.UNCHECKED,
                    label = "Leading Icon?",
                    click = { leadingIcon = !leadingIcon }
                )

                Checkbox(
                    checked = if (trailingIcon) CheckedState.CHECKED else CheckedState.UNCHECKED,
                    label = "Leading Icon?",
                    click = { trailingIcon = !trailingIcon }
                )
            }

            var value by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { value = it },
                label = "Label",
                placeholder = "Placeholder",
                error = error,
                supportingText = supportingText,
                leadingIcon = if (leadingIcon) {
                    { Icon() }
                } else {
                    null
                },
                trailingIcon = if (trailingIcon) {
                    { Icon() }
                } else {
                    null
                },
            )
        }
    }
}

private const val DEFAULT_PADDING_VALUES = 16