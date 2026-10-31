package by.rowing.sanbaiteam.training.presentation.list.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterParameter
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilterTimeParser
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.platform.rememberTrainingDatePicker
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_filter_athletes
import by.rowing.sanbaiteam.training.resources.training_filter_athletes_count
import by.rowing.sanbaiteam.training.resources.training_filter_clear_parameter
import by.rowing.sanbaiteam.training.resources.training_filter_date
import by.rowing.sanbaiteam.training.resources.training_filter_date_from_label
import by.rowing.sanbaiteam.training.resources.training_filter_date_incomplete
import by.rowing.sanbaiteam.training.resources.training_filter_date_order_invalid
import by.rowing.sanbaiteam.training.resources.training_filter_date_placeholder
import by.rowing.sanbaiteam.training.resources.training_filter_date_range_value
import by.rowing.sanbaiteam.training.resources.training_filter_date_to_label
import by.rowing.sanbaiteam.training.resources.training_filter_distance_invalid
import by.rowing.sanbaiteam.training.resources.training_filter_distance_placeholder_from
import by.rowing.sanbaiteam.training.resources.training_filter_distance_placeholder_to
import by.rowing.sanbaiteam.training.resources.training_filter_distance_range_value
import by.rowing.sanbaiteam.training.resources.training_filter_lactate
import by.rowing.sanbaiteam.training.resources.training_filter_lactate_active
import by.rowing.sanbaiteam.training.resources.training_filter_no_athletes
import by.rowing.sanbaiteam.training.resources.training_filter_piece_distance
import by.rowing.sanbaiteam.training.resources.training_filter_piece_time
import by.rowing.sanbaiteam.training.resources.training_filter_range_order_invalid
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_athletes_subtitle
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_athletes_title
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_date_subtitle
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_date_title
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_distance_subtitle
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_distance_title
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_time_subtitle
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_time_title
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_types_subtitle
import by.rowing.sanbaiteam.training.resources.training_filter_sheet_types_title
import by.rowing.sanbaiteam.training.resources.training_filter_time_invalid
import by.rowing.sanbaiteam.training.resources.training_filter_time_placeholder_from
import by.rowing.sanbaiteam.training.resources.training_filter_time_placeholder_to
import by.rowing.sanbaiteam.training.resources.training_filter_time_range_value
import by.rowing.sanbaiteam.training.resources.training_filter_training_type
import by.rowing.sanbaiteam.training.resources.training_filter_types_count
import by.rowing.sanbaiteam.training.resources.training_filters_active_count
import by.rowing.sanbaiteam.training.resources.training_filters_apply
import by.rowing.sanbaiteam.training.resources.training_filters_reset_all
import by.rowing.sanbaiteam.training.resources.training_filters_reset_parameter
import by.rowing.sanbaiteam.training.resources.training_filters_title
import by.rowing.sanbaiteam.uikit.component.BottomSheet
import by.rowing.sanbaiteam.uikit.component.Chip
import by.rowing.sanbaiteam.uikit.component.ChipRow
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.DateRangeFields
import by.rowing.sanbaiteam.uikit.component.MultiSelectList
import by.rowing.sanbaiteam.uikit.component.RangeFields
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonSize
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TrainingFiltersPanel(
    state: ListTrainingState,
    actions: ListTrainingScreenActions,
) {
    val filter = state.filter
    val athleteNames = remember(state.athleteCatalog) {
        state.athleteCatalog.associate { athlete -> athlete.id to athlete.name }
    }

    DataCard(
        modifier = Modifier.padding(horizontal = Spacing.M),
        verticalSpacing = Spacing.SM,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Spacing._2XS),
            ) {
                Text(
                    text = stringResource(Res.string.training_filters_title),
                    style = TypographyPalette.H4,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!filter.isEmpty) {
                    Text(
                        text = stringResource(
                            Res.string.training_filters_active_count,
                            filter.activeParametersCount,
                        ),
                        style = TypographyPalette.Body3Regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(
                onClick = actions::onResetFilters,
                enabled = !filter.isEmpty,
            ) {
                Text(text = stringResource(Res.string.training_filters_reset_all))
            }
        }
        ChipRow(contentPadding = PaddingValues(0.dp)) {
            for (parameter in TrainingFilterParameter.entries) {
                FilterParameterChip(
                    parameter = parameter,
                    filter = filter,
                    athleteNames = athleteNames,
                    actions = actions,
                )
            }
        }
    }
}

@Composable
internal fun TrainingFilterSheet(
    parameter: TrainingFilterParameter,
    filter: TrainingFilter,
    athleteCatalog: List<AthleteItemModel>,
    actions: ListTrainingScreenActions,
) {
    when (parameter) {
        TrainingFilterParameter.ATHLETES -> AthletesFilterSheet(
            filter = filter,
            athleteCatalog = athleteCatalog,
            actions = actions,
        )

        TrainingFilterParameter.TRAINING_TYPE -> TrainingTypesFilterSheet(
            filter = filter,
            actions = actions,
        )

        TrainingFilterParameter.PIECE_TIME -> PieceTimeFilterSheet(
            filter = filter,
            actions = actions,
        )

        TrainingFilterParameter.PIECE_DISTANCE -> PieceDistanceFilterSheet(
            filter = filter,
            actions = actions,
        )

        TrainingFilterParameter.DATE -> DateFilterSheet(
            filter = filter,
            actions = actions,
        )

        TrainingFilterParameter.LACTATE -> Unit
    }
}

@Composable
private fun FilterParameterChip(
    parameter: TrainingFilterParameter,
    filter: TrainingFilter,
    athleteNames: Map<Long, String>,
    actions: ListTrainingScreenActions,
) {
    val active = filter.isActive(parameter)
    val label = parameter.chipLabel(
        filter = filter,
        athleteNames = athleteNames
    )

    if (parameter == TrainingFilterParameter.LACTATE) {
        Chip(
            text = label,
            selected = active,
            onClick = actions::onLactateFilterToggle,
        )
    } else {
        Chip(
            text = label,
            selected = active,
            onClick = { actions.onFilterSheetOpen(parameter) },
            onClearClick = if (active) {
                { actions.onFilterReset(parameter) }
            } else {
                null
            },
            clearContentDescription = stringResource(Res.string.training_filter_clear_parameter),
        )
    }
}

@Composable
private fun AthletesFilterSheet(
    filter: TrainingFilter,
    athleteCatalog: List<AthleteItemModel>,
    actions: ListTrainingScreenActions,
) {
    var selected by remember(filter.athleteIds) { mutableStateOf(filter.athleteIds) }

    BottomSheet(
        onDismissRequest = actions::onFilterSheetDismiss,
        title = stringResource(Res.string.training_filter_sheet_athletes_title),
        subtitle = stringResource(Res.string.training_filter_sheet_athletes_subtitle),
        footer = {
            FilterSheetFooter(
                onReset = { actions.onFilterReset(TrainingFilterParameter.ATHLETES) },
                onApply = { actions.onFilterChange(filter.copy(athleteIds = selected)) },
            )
        },
    ) {
        MultiSelectList(
            items = athleteCatalog,
            selectedItems = athleteCatalog.filter { athlete -> selected.contains(athlete.id) }.toSet(),
            onItemToggle = { athlete -> selected = selected.toggled(athlete.id) },
            itemLabel = { athlete -> athlete.name },
            itemKey = { athlete -> athlete.id },
            emptyText = stringResource(Res.string.training_filter_no_athletes),
        )
    }
}

@Composable
private fun TrainingTypesFilterSheet(
    filter: TrainingFilter,
    actions: ListTrainingScreenActions,
) {
    var selected by remember(filter.trainingTypes) { mutableStateOf(filter.trainingTypes) }
    val labels = trainingTypeLabels()

    BottomSheet(
        onDismissRequest = actions::onFilterSheetDismiss,
        title = stringResource(Res.string.training_filter_sheet_types_title),
        subtitle = stringResource(Res.string.training_filter_sheet_types_subtitle),
        footer = {
            FilterSheetFooter(
                onReset = { actions.onFilterReset(TrainingFilterParameter.TRAINING_TYPE) },
                onApply = { actions.onFilterChange(filter.copy(trainingTypes = selected)) },
            )
        },
    ) {
        MultiSelectList(
            items = TrainingPieceType.entries,
            selectedItems = selected,
            onItemToggle = { type -> selected = selected.toggled(type) },
            itemLabel = { type -> labels.getValue(type) },
            itemKey = { type -> type.name },
        )
    }
}

@Composable
private fun PieceTimeFilterSheet(
    filter: TrainingFilter,
    actions: ListTrainingScreenActions,
) {
    var from by remember(filter.pieceTimeRange) {
        mutableStateOf(
            filter.pieceTimeRange
                ?.first
                ?.let(RowingTimeFormat::formatDurationMmSs)
                .orEmpty()
        )
    }
    var to by remember(filter.pieceTimeRange) {
        mutableStateOf(
            filter.pieceTimeRange
                ?.last
                ?.let(RowingTimeFormat::formatDurationMmSs)
                .orEmpty()
        )
    }

    val fromMillis = TrainingFilterTimeParser.parseMillis(from)
    val toMillis = TrainingFilterTimeParser.parseMillis(to)
    val bothFilled = from.isNotBlank() && to.isNotBlank()
    val error = when {
        !bothFilled -> null
        fromMillis == null || toMillis == null -> stringResource(Res.string.training_filter_time_invalid)
        fromMillis > toMillis -> stringResource(Res.string.training_filter_range_order_invalid)
        else -> null
    }
    val applyEnabled = fromMillis != null && toMillis != null && fromMillis <= toMillis

    BottomSheet(
        onDismissRequest = actions::onFilterSheetDismiss,
        title = stringResource(Res.string.training_filter_sheet_time_title),
        subtitle = stringResource(Res.string.training_filter_sheet_time_subtitle),
        footer = {
            FilterSheetFooter(
                onReset = { actions.onFilterReset(TrainingFilterParameter.PIECE_TIME) },
                onApply = {
                    val start = fromMillis
                    val end = toMillis
                    if (start != null && end != null) {
                        actions.onFilterChange(filter.copy(pieceTimeRange = start..end))
                    }
                },
                applyEnabled = applyEnabled,
            )
        },
    ) {
        RangeFields(
            fromValue = from,
            onFromValueChange = { from = it },
            toValue = to,
            onToValueChange = { to = it },
            label = stringResource(Res.string.training_filter_piece_time),
            fromPlaceholder = stringResource(Res.string.training_filter_time_placeholder_from),
            toPlaceholder = stringResource(Res.string.training_filter_time_placeholder_to),
            isError = error != null,
            supportingText = error,
        )
    }
}

@Composable
private fun PieceDistanceFilterSheet(
    filter: TrainingFilter,
    actions: ListTrainingScreenActions,
) {
    var from by remember(filter.pieceDistanceRange) {
        mutableStateOf(filter.pieceDistanceRange?.first?.toString().orEmpty())
    }
    var to by remember(filter.pieceDistanceRange) {
        mutableStateOf(filter.pieceDistanceRange?.last?.toString().orEmpty())
    }

    val fromMeters = from.toIntOrNull()?.takeIf { meters -> meters >= 0 }
    val toMeters = to.toIntOrNull()?.takeIf { meters -> meters >= 0 }
    val bothFilled = from.isNotBlank() && to.isNotBlank()
    val error = when {
        !bothFilled -> null
        fromMeters == null || toMeters == null -> stringResource(Res.string.training_filter_distance_invalid)
        fromMeters > toMeters -> stringResource(Res.string.training_filter_range_order_invalid)
        else -> null
    }
    val applyEnabled = fromMeters != null && toMeters != null && fromMeters <= toMeters

    BottomSheet(
        onDismissRequest = actions::onFilterSheetDismiss,
        title = stringResource(Res.string.training_filter_sheet_distance_title),
        subtitle = stringResource(Res.string.training_filter_sheet_distance_subtitle),
        footer = {
            FilterSheetFooter(
                onReset = { actions.onFilterReset(TrainingFilterParameter.PIECE_DISTANCE) },
                onApply = {
                    if (fromMeters != null && toMeters != null) {
                        actions.onFilterChange(filter.copy(pieceDistanceRange = fromMeters..toMeters))
                    }
                },
                applyEnabled = applyEnabled,
            )
        },
    ) {
        RangeFields(
            fromValue = from,
            onFromValueChange = { from = it },
            toValue = to,
            onToValueChange = { to = it },
            label = stringResource(Res.string.training_filter_piece_distance),
            fromPlaceholder = stringResource(Res.string.training_filter_distance_placeholder_from),
            toPlaceholder = stringResource(Res.string.training_filter_distance_placeholder_to),
            isError = error != null,
            supportingText = error,
        )
    }
}

@Composable
private fun DateFilterSheet(
    filter: TrainingFilter,
    actions: ListTrainingScreenActions,
) {
    var fromMillis by remember(filter.dateRange) { mutableStateOf(filter.dateRange?.first) }
    var toMillis by remember(filter.dateRange) { mutableStateOf(filter.dateRange?.last) }

    val pickFrom = rememberTrainingDatePicker(
        initialDateMillis = fromMillis ?: nowInMillis(),
        onDatePicked = { year, month, day ->
            fromMillis = TimeUtils.startOfDayMillis(
                year = year,
                month = month,
                day = day
            )
        },
    )
    val pickTo = rememberTrainingDatePicker(
        initialDateMillis = toMillis ?: nowInMillis(),
        onDatePicked = { year, month, day ->
            toMillis = TimeUtils.endOfDayMillis(
                year = year,
                month = month,
                day = day
            )
        },
    )

    val from = fromMillis
    val to = toMillis
    val orderInvalid = from != null && to != null && from > to
    val supportingText = when {
        orderInvalid -> stringResource(Res.string.training_filter_date_order_invalid)
        (from == null) != (to == null) -> stringResource(Res.string.training_filter_date_incomplete)
        else -> null
    }
    val applyEnabled = from != null && to != null && !orderInvalid

    BottomSheet(
        onDismissRequest = actions::onFilterSheetDismiss,
        title = stringResource(Res.string.training_filter_sheet_date_title),
        subtitle = stringResource(Res.string.training_filter_sheet_date_subtitle),
        footer = {
            FilterSheetFooter(
                onReset = { actions.onFilterReset(TrainingFilterParameter.DATE) },
                onApply = {
                    val start = fromMillis
                    val end = toMillis
                    if (start != null && end != null) {
                        actions.onFilterChange(filter.copy(dateRange = start..end))
                    }
                },
                applyEnabled = applyEnabled,
            )
        },
    ) {
        DateRangeFields(
            fromValue = from?.let(TimeUtils::formatDate).orEmpty(),
            onFromClick = pickFrom,
            toValue = to?.let(TimeUtils::formatDate).orEmpty(),
            onToClick = pickTo,
            label = stringResource(Res.string.training_filter_date),
            fromLabel = stringResource(Res.string.training_filter_date_from_label),
            toLabel = stringResource(Res.string.training_filter_date_to_label),
            fromPlaceholder = stringResource(Res.string.training_filter_date_placeholder),
            toPlaceholder = stringResource(Res.string.training_filter_date_placeholder),
            isError = orderInvalid,
            supportingText = supportingText,
        )
    }
}

@Composable
private fun FilterSheetFooter(
    onReset: () -> Unit,
    onApply: () -> Unit,
    applyEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onReset,
            modifier = Modifier.weight(weight = 1f),
        ) {
            Text(text = stringResource(Res.string.training_filters_reset_parameter))
        }
        Button(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(Res.string.training_filters_apply),
            debounceClick = onApply,
            enabled = applyEnabled,
            size = ButtonSize.SMALL,
        )
    }
}

@Composable
private fun TrainingFilterParameter.chipLabel(
    filter: TrainingFilter,
    athleteNames: Map<Long, String>,
): String = when (this) {
    TrainingFilterParameter.ATHLETES -> athleteChipLabel(
        athleteIds = filter.athleteIds,
        athleteNames = athleteNames,
    )

    TrainingFilterParameter.TRAINING_TYPE -> when {
        filter.trainingTypes.isEmpty() -> stringResource(Res.string.training_filter_training_type)
        filter.trainingTypes.size <= 2 -> trainingTypeLabels()
            .filterKeys { type -> type in filter.trainingTypes }
            .values
            .joinToString(separator = ", ")

        else -> stringResource(
            Res.string.training_filter_types_count,
            filter.trainingTypes.size,
        )
    }

    TrainingFilterParameter.PIECE_TIME -> filter.pieceTimeRange?.let { range ->
        stringResource(
            Res.string.training_filter_time_range_value,
            RowingTimeFormat.formatDurationMmSs(range.first),
            RowingTimeFormat.formatDurationMmSs(range.last),
        )
    } ?: stringResource(Res.string.training_filter_piece_time)

    TrainingFilterParameter.PIECE_DISTANCE -> filter.pieceDistanceRange?.let { range ->
        stringResource(
            Res.string.training_filter_distance_range_value,
            range.first,
            range.last,
        )
    } ?: stringResource(Res.string.training_filter_piece_distance)

    TrainingFilterParameter.LACTATE -> stringResource(
        if (filter.lactateOnly) Res.string.training_filter_lactate_active else Res.string.training_filter_lactate
    )

    TrainingFilterParameter.DATE -> filter.dateRange?.let { range ->
        stringResource(
            Res.string.training_filter_date_range_value,
            TimeUtils.formatDate(range.first),
            TimeUtils.formatDate(timeInMillis = range.last),
        )
    } ?: stringResource(Res.string.training_filter_date)
}

@Composable
private fun athleteChipLabel(
    athleteIds: Set<Long>,
    athleteNames: Map<Long, String>,
): String = when {
    athleteIds.isEmpty() -> stringResource(Res.string.training_filter_athletes)
    athleteIds.size == 1 -> athleteNames[athleteIds.first()] ?: stringResource(
        Res.string.training_filter_athletes_count,
        athleteIds.size
    )

    else -> stringResource(Res.string.training_filter_athletes_count, athleteIds.size)
}

@Composable
private fun trainingTypeLabels(): Map<TrainingPieceType, String> =
    TrainingPieceType.entries.associateWith { type -> stringResource(type.uiResId) }

private fun <T> Set<T>.toggled(value: T): Set<T> =
    if (value in this) this - value else this + value