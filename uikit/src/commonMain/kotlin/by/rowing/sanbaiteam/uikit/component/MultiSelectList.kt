package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.ui.tooling.preview.Preview

private val DEFAULT_MAX_HEIGHT = 360.dp

/**
 * Список с множественным выбором для нижних панелей.
 *
 * @param itemKey ключ элемента для стабильной прокрутки (обычно id).
 * @param maxHeight ограничивает высоту списка, чтобы панель не занимала весь экран.
 */
@AllowDetektPublic
@Composable
fun <T> MultiSelectList(
    items: List<T>,
    selectedItems: Set<T>,
    onItemToggle: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    itemSubtitle: ((T) -> String?)? = null,
    itemKey: ((T) -> Any)? = null,
    maxHeight: Dp = DEFAULT_MAX_HEIGHT,
    emptyText: String? = null,
) {
    if (items.isEmpty()) {
        if (emptyText != null) {
            Text(
                text = emptyText,
                modifier = modifier.padding(vertical = Spacing.S),
                style = TypographyPalette.Body2Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        verticalArrangement = Arrangement.spacedBy(Spacing._2XS),
    ) {
        this.items(
            items = items,
            key = itemKey,
        ) { item ->
            MultiSelectRow(
                label = itemLabel(item),
                subtitle = itemSubtitle?.invoke(item),
                selected = item in selectedItems,
                onClick = { onItemToggle(item) },
            )
        }
    }
}

@Composable
private fun MultiSelectRow(
    label: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Checkbox,
            )
            .padding(
                horizontal = Spacing.SM,
                vertical = Spacing.S,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(Spacing._2XS),
        ) {
            Text(
                text = label,
                style = TypographyPalette.Body2Regular,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TypographyPalette.Body3Regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Checkbox(
            checked = selected,
            onCheckedChange = null,
        )
    }
}

@Preview
@Composable
private fun MultiSelectListPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            val items = listOf("Иванов Иван", "Петров Пётр", "Сидоров Сидор")
            MultiSelectList(
                modifier = Modifier.padding(Spacing.M),
                items = items,
                selectedItems = setOf(items.first()),
                onItemToggle = {},
                itemLabel = { it },
            )
        }
    }
}