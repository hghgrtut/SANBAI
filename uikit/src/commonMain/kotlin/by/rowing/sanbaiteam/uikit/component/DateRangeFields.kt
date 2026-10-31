package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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

/**
 * Пара кликабельных полей для выбора диапазона дат.
 *
 * Поля только отображают значения; выбор даты выполняет вызывающий код.
 */
@AllowDetektPublic
@Composable
fun DateRangeFields(
    fromValue: String,
    onFromClick: () -> Unit,
    toValue: String,
    onToClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    fromLabel: String = stringResource(Res.string.uikit_range_from),
    toLabel: String = stringResource(Res.string.uikit_range_to),
    fromPlaceholder: String? = null,
    toPlaceholder: String? = null,
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
            DateField(
                modifier = Modifier.weight(weight = 1f),
                value = fromValue,
                onClick = onFromClick,
                label = fromLabel,
                placeholder = fromPlaceholder,
                isError = isError,
            )
            Text(
                text = RANGE_SEPARATOR,
                modifier = Modifier.padding(top = SEPARATOR_TOP_PADDING),
                style = TypographyPalette.Body1Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            DateField(
                modifier = Modifier.weight(weight = 1f),
                value = toValue,
                onClick = onToClick,
                label = toLabel,
                placeholder = toPlaceholder,
                isError = isError,
            )
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = TypographyPalette.Body3Regular,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun DateField(
    value: String,
    onClick: () -> Unit,
    label: String,
    placeholder: String?,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(
            role = Role.Button,
            onClick = onClick,
        )
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            error = isError,
            enabled = false,
        )
    }
}

@Preview
@Composable
private fun DateRangeFieldsPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            var fromValue by remember { mutableStateOf("01.01.2026") }
            var toValue by remember { mutableStateOf("31.01.2026") }

            DateRangeFields(
                modifier = Modifier.padding(Spacing.M),
                fromValue = fromValue,
                onFromClick = { fromValue = "01.02.2026" },
                toValue = toValue,
                onToClick = { toValue = "28.02.2026" },
                label = "Дата тренировки",
                fromPlaceholder = "Выберите дату",
                toPlaceholder = "Выберите дату",
            )
        }
    }
}