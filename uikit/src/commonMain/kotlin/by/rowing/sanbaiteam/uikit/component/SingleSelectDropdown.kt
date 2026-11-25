package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette

/**
 * Выпадающий список с единственным выбором.
 *
 * @param selectedItem текущий выбранный элемент или `null`, если выбор не сделан.
 * @param placeholder подпись, показываемая вместо значения при пустом выборе.
 */
@AllowDetektPublic
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SingleSelectDropdown(
    items: List<T>,
    itemText: @Composable (item: T) -> String,
    onItemSelected: (item: T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = selectedItem?.let { item -> itemText(item) } ?: placeholder.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = label,
            textStyle = TypographyPalette.Body1Regular,
            leadingIcon = leadingIcon,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            error = error != null,
            supportingText = error,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemText(item),
                            style = TypographyPalette.Body1Regular,
                        )
                    },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    },
                )
            }
        }
    }
}