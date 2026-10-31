package by.rowing.sanbaiteam.training.presentation.list.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import by.rowing.sanbaiteam.athlete.presentation.common.ItemCard
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.filter.applyTrainingFilter
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import by.rowing.sanbaiteam.training.presentation.platform.rememberCreateBackupLauncher
import by.rowing.sanbaiteam.training.presentation.platform.rememberOpenBackupLauncher
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.cancel
import by.rowing.sanbaiteam.training.resources.ok
import by.rowing.sanbaiteam.training.resources.training_add_some_training
import by.rowing.sanbaiteam.training.resources.training_add_training
import by.rowing.sanbaiteam.training.resources.training_delete_action
import by.rowing.sanbaiteam.training.resources.training_delete_confirm_message
import by.rowing.sanbaiteam.training.resources.training_delete_confirm_title
import by.rowing.sanbaiteam.training.resources.training_filter_active_badge
import by.rowing.sanbaiteam.training.resources.training_filter_content_description
import by.rowing.sanbaiteam.training.resources.training_filters_empty_action
import by.rowing.sanbaiteam.training.resources.training_filters_empty_description
import by.rowing.sanbaiteam.training.resources.training_filters_empty_title
import by.rowing.sanbaiteam.training.resources.training_list_title
import by.rowing.sanbaiteam.training.resources.training_no_trainings
import by.rowing.sanbaiteam.training.resources.training_selection_cancel
import by.rowing.sanbaiteam.training.resources.training_selection_clear_all
import by.rowing.sanbaiteam.training.resources.training_selection_select_all
import by.rowing.sanbaiteam.training.resources.training_selection_title
import by.rowing.sanbaiteam.training.resources.training_transfer_export_selected_action
import by.rowing.sanbaiteam.training.resources.training_transfer_import_action
import by.rowing.sanbaiteam.training.resources.training_transfer_notice_title
import by.rowing.sanbaiteam.uikit.component.Badge
import by.rowing.sanbaiteam.uikit.component.Checkbox
import by.rowing.sanbaiteam.uikit.component.CheckedState
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Skeleton
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.UiText
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import androidx.compose.material3.Badge as MaterialBadge

private const val CONTENT_FADE_IN_MILLIS = 220
private const val CONTENT_FADE_OUT_MILLIS = 140
private const val CHECKBOX_ENTER_SCALE = 0.7f

private val SKELETON_BADGE_WIDTH = 88.dp
private val SKELETON_BADGE_HEIGHT = 20.dp
private val SKELETON_DATE_WIDTH = 72.dp
private val SKELETON_DATE_HEIGHT = 14.dp
private val SKELETON_TITLE_HEIGHT = 28.dp
private val SKELETON_SUBTITLE_HEIGHT = 16.dp
private const val SKELETON_TITLE_FRACTION = 0.55f
private const val SKELETON_SUBTITLE_FRACTION = 0.35f
private const val SKELETON_ITEM_COUNT = 5

@Composable
internal fun ListTrainingScreen(
    viewModel: ListTrainingViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.reloadTrainings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ListTrainingScreenContent(
        state = viewModel.state,
        actions = viewModel
    )
}

@Composable
private fun ListTrainingScreenContent(
    state: ListTrainingState,
    actions: ListTrainingScreenActions
) {
    val filteredTrainings = remember(state.filter, state.items) {
        state.items.applyTrainingFilter(trainingFilter = state.filter)
    }

    val createBackupLauncher = rememberCreateBackupLauncher(onCreated = actions::onExportToHandle)
    val openBackupLauncher = rememberOpenBackupLauncher(onOpened = actions::onImportFromHandle)

    LaunchedEffect(state.exportPromptRequested) {
        if (state.exportPromptRequested) {
            createBackupLauncher(suggestedBackupFileName())
            actions.onExportPromptLaunched()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ListTrainingTopBar(
                actions = actions,
                openBackupLauncher = openBackupLauncher,
                state = state,
                visibleTrainingIds = filteredTrainings.map { training -> training.trainingId },
            )
        },
        floatingActionButton = { AddTrainingButton(state = state) { actions.onAddTrainingClick() } }
    ) { paddingValues ->
        TrainingListMainContent(
            modifier = Modifier.padding(paddingValues = paddingValues),
            state = state,
            actions = actions,
            filteredTrainings = filteredTrainings
        )
    }
    state.activeFilterSheet?.let { parameter ->
        TrainingFilterSheet(
            parameter = parameter,
            filter = state.filter,
            athleteCatalog = state.athleteCatalog,
            actions = actions,
        )
    }
    TrainingQuickFilterDialogs(
        state = state,
        actions = actions,
    )
    state.transferNotice?.let { notice ->
        TransferDataAlert(
            actions = actions,
            notice = notice
        )
    }
    if (state.deleteConfirmationRequested) {
        DeleteTrainingsConfirmDialog(
            selectedCount = state.selectedTrainingIds.size,
            onDismiss = actions::onDeleteDismiss,
            onConfirm = actions::onDeleteConfirm
        )
    }
}

@Composable
private fun ListTrainingTopBar(
    state: ListTrainingState,
    visibleTrainingIds: List<Long>,
    openBackupLauncher: () -> Unit,
    actions: ListTrainingScreenActions
) {
    if (state.isSelectionMode) {
        SelectionTopBar(
            state = state,
            visibleTrainingIds = visibleTrainingIds,
            actions = actions
        )
    } else {
        ListTrainingTopBarContent(
            state = state,
            openBackupLauncher = openBackupLauncher,
            actions = actions
        )
    }
}

@Composable
private fun SelectionTopBar(
    state: ListTrainingState,
    visibleTrainingIds: List<Long>,
    actions: ListTrainingScreenActions
) {
    val allVisibleSelected = visibleTrainingIds.isNotEmpty() &&
            visibleTrainingIds.all { trainingId -> trainingId in state.selectedTrainingIds }
    TopAppBar(
        title = stringResource(Res.string.training_selection_title, state.selectedTrainingIds.size),
        navigationIcon = {
            IconButton(onClick = actions::onCancelSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.training_selection_cancel)
                )
            }
        },
        actions = {
            FilterActionButton(
                state = state,
                actions = actions,
            )
            IconButton(
                onClick = { actions.onToggleSelectAllClick(visibleTrainingIds) },
                enabled = visibleTrainingIds.isNotEmpty()
            ) {
                Icon(
                    imageVector = if (allVisibleSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                    contentDescription = stringResource(
                        if (allVisibleSelected) {
                            Res.string.training_selection_clear_all
                        } else {
                            Res.string.training_selection_select_all
                        }
                    )
                )
            }
            IconButton(
                onClick = actions::onExportSelectedClick,
                enabled = !state.isTransferring
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(Res.string.training_transfer_export_selected_action)
                )
            }
            IconButton(onClick = actions::onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.training_delete_action)
                )
            }
        }
    )
}

@Composable
private fun ListTrainingTopBarContent(
    state: ListTrainingState,
    openBackupLauncher: () -> Unit,
    actions: ListTrainingScreenActions
) {
    TopAppBar(
        title = stringResource(Res.string.training_list_title),
        actions = {
            IconButton(
                onClick = openBackupLauncher,
                enabled = !state.isTransferring
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = stringResource(Res.string.training_transfer_import_action)
                )
            }
            FilterActionButton(
                state = state,
                actions = actions,
            )
        },
        onNavIconClick = actions::navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterActionButton(
    state: ListTrainingState,
    actions: ListTrainingScreenActions,
) {
    val activeCount = state.filter.activeParametersCount
    val isHighlighted = state.showFilters || activeCount > 0
    IconButton(onClick = { actions.onShowFiltersChange(!state.showFilters) }) {
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
                tint = MaterialTheme.colorScheme.run { if (isHighlighted) primary else onSurfaceVariant }
            )
        }
    }
}

@Composable
private fun TrainingListMainContent(
    modifier: Modifier,
    state: ListTrainingState,
    actions: ListTrainingScreenActions,
    filteredTrainings: List<ListTrainingStateItem>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S)
    ) {
        if (state.showFilters) {
            TrainingFiltersPanel(
                state = state,
                actions = actions,
            )
        }
        val contentState = when {
            state.isLoading -> TrainingListContentState.LOADING
            filteredTrainings.isEmpty() -> TrainingListContentState.EMPTY
            else -> TrainingListContentState.CONTENT
        }
        AnimatedContent(
            targetState = contentState,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = CONTENT_FADE_IN_MILLIS)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = CONTENT_FADE_OUT_MILLIS))
            },
            label = "trainingListContent"
        ) { targetState ->
            when (targetState) {
                TrainingListContentState.LOADING -> TrainingListSkeleton()
                TrainingListContentState.EMPTY -> EmptyTrainingList(
                    filteredByFilter = state.items.isNotEmpty(),
                    actions = actions
                )

                TrainingListContentState.CONTENT -> TrainingList(
                    trainings = filteredTrainings,
                    selectedTrainingIds = state.selectedTrainingIds,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun AddTrainingButton(
    state: ListTrainingState,
    onAddTrainingClick: () -> Unit
) {
    AnimatedVisibility(
        visible = !state.isSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) { AddTrainingFAB(onAddTraining = onAddTrainingClick) }
}

@Composable
private fun ListTrainingItem(
    modifier: Modifier = Modifier,
    item: ListTrainingStateItem,
    isSelectionMode: Boolean,
    selected: Boolean,
    onTrainingClick: (ListTrainingStateItem) -> Unit,
    onTrainingLongClick: (ListTrainingStateItem) -> Unit
) {
    ItemCard(
        modifier = modifier,
        onClick = { onTrainingClick(item) },
        onLongClick = { onTrainingLongClick(item) },
        selected = selected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.S),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.XS)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TrainingTypeBadge(type = item.trainingType)
                    Text(
                        text = item.trainingDate,
                        style = TypographyPalette.Caption1Regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = item.trainingWork,
                    style = TypographyPalette.H1,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.athletes.isNotEmpty()) {
                    Text(
                        text = item.athletes.joinToString(separator = ", "),
                        style = TypographyPalette.Body2Regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn() + scaleIn(initialScale = CHECKBOX_ENTER_SCALE),
                exit = fadeOut() + scaleOut(targetScale = CHECKBOX_ENTER_SCALE),
            ) {
                Checkbox(
                    checked = if (selected) CheckedState.CHECKED else CheckedState.UNCHECKED,
                    click = { onTrainingClick(item) }
                )
            }
        }
    }
}

@Composable
private fun TrainingTypeBadge(type: TrainingPieceType) {
    val colors = when (type) {
        TrainingPieceType.CONCEPT_ROW_ERG,
        TrainingPieceType.RP_3,
        TrainingPieceType.CONCEPT_BIKE_ERG,
        TrainingPieceType.WATT_BIKE -> ColorPalette.Blue95 to ColorPalette.Blue20

        TrainingPieceType.SINGLE,
        TrainingPieceType.DOUBLE,
        TrainingPieceType.QUADRUPLE,
        TrainingPieceType.PAIR,
        TrainingPieceType.FOUR,
        TrainingPieceType.EIGHT -> ColorPalette.Green50 to ColorPalette.Grey10
    }
    Badge(
        text = stringResource(type.uiResId),
        containerColor = colors.first,
        contentColor = colors.second,
    )
}

@Composable
private fun TrainingList(
    trainings: List<ListTrainingStateItem>,
    selectedTrainingIds: Set<Long>,
    actions: ListTrainingScreenActions
) {
    val isSelectionMode = selectedTrainingIds.isNotEmpty()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
        contentPadding = PaddingValues(vertical = Spacing.S)
    ) {
        items(
            items = trainings,
            key = { it.trainingId }
        ) { training ->
            ListTrainingItem(
                modifier = Modifier.animateItem(),
                item = training,
                isSelectionMode = isSelectionMode,
                selected = training.trainingId in selectedTrainingIds,
                onTrainingClick = { actions.onTrainingClick(training.trainingId) },
                onTrainingLongClick = { actions.onTrainingLongClick(training.trainingId) }
            )
        }
    }
}

@Composable
private fun TrainingListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Spacing.S),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
    ) {
        repeat(times = SKELETON_ITEM_COUNT) {
            TrainingSkeletonItem()
        }
    }
}

@Composable
private fun TrainingSkeletonItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.M),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Skeleton(
                    modifier = Modifier.size(
                        width = SKELETON_BADGE_WIDTH,
                        height = SKELETON_BADGE_HEIGHT,
                    )
                )
                Skeleton(
                    modifier = Modifier.size(
                        width = SKELETON_DATE_WIDTH,
                        height = SKELETON_DATE_HEIGHT,
                    )
                )
            }
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth(fraction = SKELETON_TITLE_FRACTION)
                    .height(SKELETON_TITLE_HEIGHT)
            )
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth(fraction = SKELETON_SUBTITLE_FRACTION)
                    .height(SKELETON_SUBTITLE_HEIGHT)
            )
        }
    }
}

@Composable
private fun EmptyTrainingList(
    filteredByFilter: Boolean,
    actions: ListTrainingScreenActions
) {
    if (filteredByFilter) {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.training_filters_empty_title),
            description = stringResource(Res.string.training_filters_empty_description),
            icon = Icons.Default.FilterList,
            actionText = stringResource(Res.string.training_filters_empty_action),
            onActionClick = actions::onResetFilters,
        )
    } else {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.training_no_trainings),
            description = stringResource(Res.string.training_add_some_training),
            icon = Icons.Default.TableRows,
            actionText = stringResource(Res.string.training_add_training),
            onActionClick = actions::onAddTrainingClick,
        )
    }
}

@Composable
private fun AddTrainingFAB(onAddTraining: () -> Unit) {
    FloatingActionButton(
        onClick = onAddTraining,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(Spacing.M)
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = stringResource(Res.string.training_add_training)
        )
    }
}

@Composable
private fun TransferDataAlert(
    actions: ListTrainingScreenActions,
    notice: UiText
) {
    AlertDialog(
        onDismissRequest = actions::onDismissTransferNotice,
        title = { Text(text = stringResource(Res.string.training_transfer_notice_title)) },
        text = { Text(text = notice.resolve()) },
        confirmButton = {
            TextButton(onClick = actions::onDismissTransferNotice) { Text(text = stringResource(Res.string.ok)) }
        }
    )
}

@Composable
private fun DeleteTrainingsConfirmDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.training_delete_confirm_title),
                style = TypographyPalette.H4
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.training_delete_confirm_message, selectedCount),
                style = TypographyPalette.Body1Regular
            )
        },
        confirmButton = {
            Button(
                text = stringResource(Res.string.training_delete_action),
                buttonColors = ButtonColors.attention,
                debounceClick = onConfirm
            )
        },
        dismissButton = {
            Button(
                text = stringResource(Res.string.cancel),
                buttonColors = ButtonColors.text(),
                debounceClick = onDismiss
            )
        }
    )
}

private enum class TrainingListContentState {
    LOADING,
    EMPTY,
    CONTENT,
}

private fun suggestedBackupFileName(): String {
    val date = Instant.fromEpochMilliseconds(nowInMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
    val month = date.month.number.toString().padStart(2, '0')
    val day = date.day.toString().padStart(2, '0')
    return "sanbai-training-backup-${date.year}$month$day.json"
}