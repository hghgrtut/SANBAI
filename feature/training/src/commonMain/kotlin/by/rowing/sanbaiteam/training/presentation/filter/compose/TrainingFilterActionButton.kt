package by.rowing.sanbaiteam.training.presentation.filter.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_filter_active_badge
import by.rowing.sanbaiteam.training.resources.training_filter_content_description
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.Badge as MaterialBadge

/** Кнопка фильтра в тулбаре с бейджем количества активных параметров. */
@Composable
internal fun TrainingFilterActionButton(
    activeCount: Int,
    showFilters: Boolean,
    onToggle: () -> Unit,
) {
    val isHighlighted = showFilters || activeCount > 0
    IconButton(onClick = onToggle) {
        BadgedBox(
            badge = {
                if (activeCount > 0) {
                    MaterialBadge {
                        Text(text = activeCount.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = if (activeCount > 0) {
                    stringResource(Res.string.training_filter_active_badge, activeCount)
                } else {
                    stringResource(Res.string.training_filter_content_description)
                },
                tint = MaterialTheme.colorScheme.run {
                    if (isHighlighted) primary else onSurfaceVariant
                }
            )
        }
    }
}