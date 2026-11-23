package by.rowing.sanbaiteam.athlete.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_add_title
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_date_label
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_value_label
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_value_placeholder
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.UiText
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.component.decimal
import by.rowing.sanbaiteam.uikit.component.rememberDatePicker
import by.rowing.sanbaiteam.uikit.resources.Res as UikitRes
import by.rowing.sanbaiteam.uikit.resources.uikit_cancel
import by.rowing.sanbaiteam.uikit.resources.uikit_save
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

/**
 * Диалог добавления записи веса.
 *
 * @param contentBeforeFields дополнительный контент над полями даты и веса (например, выбор спортсмена на дашборде).
 */
@AllowDetektPublic
@Composable
fun AthleteWeightDialog(
    dateMillis: Long,
    weightText: String,
    error: UiText?,
    onDateChange: (year: Int, month: Int, day: Int) -> Unit,
    onWeightChange: (text: String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    contentBeforeFields: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val pickDate = rememberDatePicker(
        initialDateMillis = dateMillis,
        onDatePicked = onDateChange,
    )
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.athletes_weight_add_title),
                style = TypographyPalette.H4,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                contentBeforeFields?.invoke(this)
                DateField(
                    label = stringResource(Res.string.athletes_weight_date_label),
                    value = TimeUtils.formatDate(timeInMillis = dateMillis),
                    onClick = pickDate,
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = weightText,
                    onValueChange = onWeightChange,
                    label = stringResource(Res.string.athletes_weight_value_label),
                    placeholder = stringResource(Res.string.athletes_weight_value_placeholder),
                    error = error != null,
                    supportingText = error?.resolve(),
                    keyboardOptions = KeyboardOptions.decimal(imeAction = ImeAction.Done),
                )
            }
        },
        confirmButton = {
            Button(
                text = stringResource(UikitRes.string.uikit_save),
                debounceClick = onSave,
            )
        },
        dismissButton = {
            Button(
                text = stringResource(UikitRes.string.uikit_cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = onDismiss,
            )
        },
    )
}