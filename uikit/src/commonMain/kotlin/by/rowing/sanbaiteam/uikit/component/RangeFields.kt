package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.resources.Res
import by.rowing.sanbaiteam.uikit.resources.uikit_range_from
import by.rowing.sanbaiteam.uikit.resources.uikit_range_to
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val RANGE_SEPARATOR = "—"
private val SEPARATOR_TOP_PADDING = Spacing.SM

/** Пара полей для ввода диапазона: «от» и «до». */
@AllowDetektPublic
@Composable
fun RangeFields(
    fromValue: String,
    onFromValueChange: (String) -> Unit,
    toValue: String,
    onToValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    fromLabel: String = stringResource(Res.string.uikit_range_from),
    toLabel: String = stringResource(Res.string.uikit_range_to),
    fromPlaceholder: String? = null,
    toPlaceholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.Top,
        ) {
            TextField(
                modifier = Modifier.weight(weight = 1f),
                value = fromValue,
                onValueChange = onFromValueChange,
                label = fromLabel,
                placeholder = fromPlaceholder,
                error = isError,
                keyboardOptions = keyboardOptions,
            )
            Text(
                text = RANGE_SEPARATOR,
                modifier = Modifier.padding(top = SEPARATOR_TOP_PADDING),
                style = TypographyPalette.Body1Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextField(
                modifier = Modifier.weight(weight = 1f),
                value = toValue,
                onValueChange = onToValueChange,
                label = toLabel,
                placeholder = toPlaceholder,
                error = isError,
                keyboardOptions = keyboardOptions,
            )
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = TypographyPalette.Body3Regular,
                color = MaterialTheme.colorScheme.run { if (isError) error else onSurfaceVariant },
            )
        }
    }
}

@Preview
@Composable
private fun RangeFieldsPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            var fromValue by remember { mutableStateOf("") }
            var toValue by remember { mutableStateOf("") }

            RangeFields(
                modifier = Modifier.padding(Spacing.M),
                fromValue = fromValue,
                onFromValueChange = { fromValue = it },
                toValue = toValue,
                onToValueChange = { toValue = it },
                label = "Дистанция отрезка, м",
                fromPlaceholder = "500",
                toPlaceholder = "2000",
            )
        }
    }
}