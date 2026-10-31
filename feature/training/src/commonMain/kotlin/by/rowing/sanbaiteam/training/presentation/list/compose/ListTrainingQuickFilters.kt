package by.rowing.sanbaiteam.training.presentation.list.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.training.data.filter.SavedTrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.list.models.QuickFilterDialog
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.cancel
import by.rowing.sanbaiteam.training.resources.save
import by.rowing.sanbaiteam.training.resources.training_quick_filter_delete_action
import by.rowing.sanbaiteam.training.resources.training_quick_filter_delete_confirm_message
import by.rowing.sanbaiteam.training.resources.training_quick_filter_delete_confirm_title
import by.rowing.sanbaiteam.training.resources.training_quick_filter_manage_hint
import by.rowing.sanbaiteam.training.resources.training_quick_filter_manage_subtitle
import by.rowing.sanbaiteam.training.resources.training_quick_filter_manage_title
import by.rowing.sanbaiteam.training.resources.training_quick_filter_name_label
import by.rowing.sanbaiteam.training.resources.training_quick_filter_name_placeholder
import by.rowing.sanbaiteam.training.resources.training_quick_filter_name_taken
import by.rowing.sanbaiteam.training.resources.training_quick_filter_overwrite_confirm
import by.rowing.sanbaiteam.training.resources.training_quick_filter_overwrite_message
import by.rowing.sanbaiteam.training.resources.training_quick_filter_overwrite_title
import by.rowing.sanbaiteam.training.resources.training_quick_filter_save_subtitle
import by.rowing.sanbaiteam.training.resources.training_quick_filter_save_title
import by.rowing.sanbaiteam.training.resources.training_quick_filter_summary_title
import by.rowing.sanbaiteam.uikit.component.BottomSheet
import by.rowing.sanbaiteam.uikit.component.Chip
import by.rowing.sanbaiteam.uikit.component.ChipRow
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.component.button.ButtonSize
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private const val QUICK_FILTER_NAME_MAX_LENGTH = 40

@Composable
internal fun TrainingQuickFiltersRow(
    savedFilters: List<SavedTrainingFilter>,
    activeQuickFilterId: Long?,
    actions: ListTrainingScreenActions,
) {
    if (savedFilters.isEmpty()) return
    ChipRow(contentPadding = PaddingValues(0.dp)) {
        savedFilters.forEach { savedFilter ->
            Chip(
                text = savedFilter.name,
                selected = savedFilter.id == activeQuickFilterId,
                onClick = { actions.onQuickFilterApply(savedFilter.id) },
                onLongClick = { actions.onQuickFilterManageClick(savedFilter.id) },
                longClickLabel = stringResource(Res.string.training_quick_filter_manage_hint),
            )
        }
    }
}

@Composable
internal fun TrainingQuickFilterDialogs(
    state: ListTrainingState,
    actions: ListTrainingScreenActions,
) {
    when (val dialog = state.quickFilterDialog) {
        QuickFilterDialog.Save -> QuickFilterSaveSheet(
            filter = state.filter,
            athleteCatalog = state.athleteCatalog,
            actions = actions,
        )

        is QuickFilterDialog.Manage -> {
            val savedFilter = state.savedFilters.firstOrNull { saved -> saved.id == dialog.filterId }
            if (savedFilter != null) {
                QuickFilterManageSheet(
                    savedFilter = savedFilter,
                    otherNames = state.savedFilters
                        .filter { saved -> saved.id != savedFilter.id }
                        .map { saved -> saved.name },
                    athleteCatalog = state.athleteCatalog,
                    actions = actions,
                )
            }
        }

        null -> Unit
    }

    state.quickFilterOverwrite?.let { overwrite ->
        QuickFilterOverwriteDialog(
            name = overwrite.name,
            onConfirm = actions::onQuickFilterOverwriteConfirm,
            onDismiss = actions::onQuickFilterOverwriteDismiss,
        )
    }

    state.quickFilterDeleteId?.let { filterId ->
        val name = state.savedFilters.firstOrNull { saved -> saved.id == filterId }?.name.orEmpty()
        QuickFilterDeleteDialog(
            name = name,
            onConfirm = actions::onQuickFilterDeleteConfirm,
            onDismiss = actions::onQuickFilterDeleteDismiss,
        )
    }
}

@Composable
private fun QuickFilterSaveSheet(
    filter: TrainingFilter,
    athleteCatalog: List<AthleteItemModel>,
    actions: ListTrainingScreenActions,
) {
    var name by remember { mutableStateOf("") }
    val athleteNames = remember(athleteCatalog) {
        athleteCatalog.associate { athlete -> athlete.id to athlete.name }
    }

    BottomSheet(
        onDismissRequest = actions::onQuickFilterDialogDismiss,
        title = stringResource(Res.string.training_quick_filter_save_title),
        subtitle = stringResource(Res.string.training_quick_filter_save_subtitle),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = actions::onQuickFilterDialogDismiss,
                    modifier = Modifier.weight(weight = 1f),
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
                Button(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(Res.string.save),
                    debounceClick = { actions.onQuickFilterSave(name) },
                    enabled = name.isNotBlank(),
                    size = ButtonSize.SMALL,
                )
            }
        },
    ) {
        QuickFilterNameField(
            value = name,
            onValueChange = { value -> name = value.take(QUICK_FILTER_NAME_MAX_LENGTH) },
        )
        QuickFilterSummary(
            filter = filter,
            athleteNames = athleteNames
        )
    }
}

@Composable
private fun QuickFilterManageSheet(
    savedFilter: SavedTrainingFilter,
    otherNames: List<String>,
    athleteCatalog: List<AthleteItemModel>,
    actions: ListTrainingScreenActions,
) {
    var name by remember(savedFilter.id) { mutableStateOf(savedFilter.name) }
    val normalizedName = name.trim()
    val nameTaken = otherNames.any { otherName -> otherName.equals(normalizedName, ignoreCase = true) }
    val athleteNames = remember(athleteCatalog) {
        athleteCatalog.associate { athlete -> athlete.id to athlete.name }
    }

    BottomSheet(
        onDismissRequest = actions::onQuickFilterDialogDismiss,
        title = stringResource(Res.string.training_quick_filter_manage_title),
        subtitle = stringResource(Res.string.training_quick_filter_manage_subtitle),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.S),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { actions.onQuickFilterDeleteClick(savedFilter.id) },
                    modifier = Modifier.weight(weight = 1f),
                ) {
                    Text(
                        text = stringResource(Res.string.training_quick_filter_delete_action),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(Res.string.save),
                    debounceClick = { actions.onQuickFilterRename(savedFilter.id, name) },
                    enabled = normalizedName.isNotEmpty() && !nameTaken,
                    size = ButtonSize.SMALL,
                )
            }
        },
    ) {
        QuickFilterNameField(
            value = name,
            onValueChange = { value -> name = value.take(QUICK_FILTER_NAME_MAX_LENGTH) },
            isError = nameTaken,
            supportingText = if (nameTaken) {
                stringResource(Res.string.training_quick_filter_name_taken)
            } else {
                null
            },
        )
        QuickFilterSummary(
            filter = savedFilter.filter,
            athleteNames = athleteNames
        )
    }
}

@Composable
private fun QuickFilterNameField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = { Text(text = stringResource(Res.string.training_quick_filter_name_label)) },
        placeholder = { Text(text = stringResource(Res.string.training_quick_filter_name_placeholder)) },
        isError = isError,
        supportingText = supportingText?.let { text ->
            { Text(text = text) }
        },
        singleLine = true,
    )
}

@Composable
private fun QuickFilterSummary(
    filter: TrainingFilter,
    athleteNames: Map<Long, String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
        Text(
            text = stringResource(Res.string.training_quick_filter_summary_title),
            style = TypographyPalette.Caption1Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = filterSummary(filter = filter, athleteNames = athleteNames),
            style = TypographyPalette.Body2Regular,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun filterSummary(
    filter: TrainingFilter,
    athleteNames: Map<Long, String>,
): String {
    val labels = mutableListOf<String>()
    for (parameter in TrainingFilterParameter.entries) {
        if (filter.isActive(parameter)) {
            labels += parameter.chipLabel(filter = filter, athleteNames = athleteNames)
        }
    }
    return labels.joinToString(separator = " · ")
}

@Composable
private fun QuickFilterOverwriteDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.training_quick_filter_overwrite_title))
        },
        text = {
            Text(
                text = stringResource(
                    Res.string.training_quick_filter_overwrite_message,
                    name,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(Res.string.training_quick_filter_overwrite_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun QuickFilterDeleteDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.training_quick_filter_delete_confirm_title),
                style = TypographyPalette.H4,
            )
        },
        text = {
            Text(text = stringResource(Res.string.training_quick_filter_delete_confirm_message, name))
        },
        confirmButton = {
            Button(
                text = stringResource(Res.string.training_quick_filter_delete_action),
                buttonColors = ButtonColors.attention,
                debounceClick = onConfirm,
            )
        },
        dismissButton = {
            Button(
                text = stringResource(Res.string.cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = onDismiss,
            )
        },
    )
}