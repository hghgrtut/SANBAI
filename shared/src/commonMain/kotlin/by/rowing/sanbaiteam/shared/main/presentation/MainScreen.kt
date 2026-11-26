package by.rowing.sanbaiteam.shared.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteWeightDialog
import by.rowing.sanbaiteam.core.file.rememberCreateTextFileLauncher
import by.rowing.sanbaiteam.core.file.rememberOpenTextFileLauncher
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.shared.resources.Res
import by.rowing.sanbaiteam.shared.resources.calculator_title
import by.rowing.sanbaiteam.shared.resources.main_add_training
import by.rowing.sanbaiteam.shared.resources.main_add_weight
import by.rowing.sanbaiteam.shared.resources.main_empty_trainings_hint
import by.rowing.sanbaiteam.shared.resources.main_empty_trainings_title
import by.rowing.sanbaiteam.shared.resources.main_recent_trainings
import by.rowing.sanbaiteam.shared.resources.main_settings
import by.rowing.sanbaiteam.shared.resources.main_show_all
import by.rowing.sanbaiteam.shared.resources.main_stats_athletes
import by.rowing.sanbaiteam.shared.resources.main_stats_trainings
import by.rowing.sanbaiteam.shared.resources.main_subtitle
import by.rowing.sanbaiteam.shared.resources.main_sync_export
import by.rowing.sanbaiteam.shared.resources.main_sync_import
import by.rowing.sanbaiteam.shared.resources.main_sync_notice_title
import by.rowing.sanbaiteam.shared.resources.main_sync_title
import by.rowing.sanbaiteam.shared.resources.main_title
import by.rowing.sanbaiteam.shared.resources.main_weight_athlete_label
import by.rowing.sanbaiteam.shared.resources.main_weight_athlete_placeholder
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboardItem
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.component.DateRangeFields
import by.rowing.sanbaiteam.uikit.component.SectionHeader
import by.rowing.sanbaiteam.uikit.component.SingleSelectDropdown
import by.rowing.sanbaiteam.uikit.component.StatCard
import by.rowing.sanbaiteam.uikit.component.UiText
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.component.button.ButtonColors
import by.rowing.sanbaiteam.uikit.component.button.ButtonSize
import by.rowing.sanbaiteam.uikit.component.rememberDatePicker
import by.rowing.sanbaiteam.uikit.resources.uikit_cancel
import by.rowing.sanbaiteam.uikit.resources.uikit_ok
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.SANBAITeamTheme
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import by.rowing.sanbaiteam.uikit.resources.Res as UikitRes

private const val LOADING_VALUE = "—"

@Composable
internal fun MainScreen(viewModel: MainViewModel) {
    MainScreenContent(
        state = viewModel.state,
        actions = viewModel,
    )
}

@Composable
private fun MainScreenContent(
    state: MainUiState,
    actions: MainScreenActions,
) {
    val createSyncBackupLauncher = rememberCreateTextFileLauncher(onCreated = actions::onSyncExportToHandle)
    val openSyncBackupLauncher = rememberOpenTextFileLauncher(onOpened = actions::onSyncImportFromHandle)

    LaunchedEffect(state.syncDialog?.exportPromptRequested) {
        val dialog = state.syncDialog ?: return@LaunchedEffect
        val exportFileName =
            "sanbai-backup-${TimeUtils.formatDateDigits(dialog.fromMillis)}-${TimeUtils.formatDateDigits(dialog.toMillis)}.json"
        if (dialog.exportPromptRequested) {
            createSyncBackupLauncher(exportFileName)
            actions.onSyncExportPromptLaunched()
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(all = Spacing.M),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.L),
        ) {
            DashboardHeader(
                onSyncClick = actions::openSyncDialog,
                onSettingsClick = actions::navigateToSettings,
            )
            StatsSection(
                state = state,
                actions = actions,
            )
            QuickActionsSection(
                state = state,
                actions = actions,
            )
            RecentTrainingsSection(
                state = state,
                actions = actions,
            )
        }
    }
    WeightDialog(
        state = state,
        actions = actions,
    )
    SyncDialog(
        state = state,
        actions = actions,
        openBackupLauncher = openSyncBackupLauncher,
    )
    state.syncNotice?.let { notice ->
        SyncNotice(
            notice = notice,
            actions = actions,
        )
    }
}

@Composable
private fun DashboardHeader(
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        ColorPalette.Blue60,
                    )
                )
            )
            .padding(Spacing.L),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
            ) {
                Text(
                    text = stringResource(Res.string.main_title),
                    style = TypographyPalette.H1,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = stringResource(Res.string.main_subtitle),
                    style = TypographyPalette.Body2Medium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = HEADER_SUBTITLE_ALPHA),
                )
            }
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = stringResource(Res.string.main_sync_title),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(Res.string.main_settings),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    state: MainUiState,
    actions: MainScreenActions,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
        StatCard(
            modifier = Modifier.weight(weight = 1f),
            value = if (state.isLoading) LOADING_VALUE else state.athletes.size.toString(),
            label = stringResource(Res.string.main_stats_athletes),
            icon = Icons.Default.People,
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = actions::navigateToAthletesScreen,
        )
        StatCard(
            modifier = Modifier.weight(weight = 1f),
            value = if (state.isLoading) LOADING_VALUE else state.trainingsCount.toString(),
            label = stringResource(Res.string.main_stats_trainings),
            icon = Icons.Default.FitnessCenter,
            accentColor = MaterialTheme.colorScheme.secondary,
            onClick = actions::navigateToTrainingsScreen,
        )
    }
}

@Composable
private fun QuickActionsSection(
    state: MainUiState,
    actions: MainScreenActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Button(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(Res.string.main_add_weight),
                size = ButtonSize.SMALL,
                enabled = state.athletes.isNotEmpty(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                    )
                },
                buttonColors = ButtonColors.primary(
                    enabledText = MaterialTheme.colorScheme.onPrimary,
                    enabledBackground = MaterialTheme.colorScheme.primary,
                    disabledText = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBackground = MaterialTheme.colorScheme.surfaceVariant,
                ),
                debounceClick = actions::openWeightDialog,
            )
            Button(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(Res.string.calculator_title),
                size = ButtonSize.SMALL,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                    )
                },
                buttonColors = ButtonColors.secondary(
                    enabledText = MaterialTheme.colorScheme.onSurface,
                    enabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledText = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBackground = MaterialTheme.colorScheme.surfaceVariant,
                ),
                debounceClick = actions::navigateToCalculatorScreen,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.main_add_training),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
            buttonColors = ButtonColors.primary(
                enabledText = MaterialTheme.colorScheme.onPrimary,
                enabledBackground = MaterialTheme.colorScheme.primary,
                disabledText = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBackground = MaterialTheme.colorScheme.surfaceVariant,
            ),
            debounceClick = actions::navigateToAddTrainingScreen,
        )
    }
}

@Composable
private fun WeightDialog(
    state: MainUiState,
    actions: MainScreenActions,
) {
    val dialog = state.weightDialog ?: return
    AthleteWeightDialog(
        dateMillis = dialog.dateMillis,
        weightText = dialog.weightText,
        error = dialog.error,
        onDateChange = actions::changeWeightDate,
        onWeightChange = actions::changeWeightValue,
        onDismiss = actions::dismissWeightDialog,
        onSave = actions::saveWeight,
        contentBeforeFields = {
            SingleSelectDropdown(
                selectedItem = state.athletes.firstOrNull { athlete -> athlete.id == dialog.athleteId },
                items = state.athletes,
                itemText = { athlete -> athlete.name },
                onItemSelected = { athlete -> actions.changeWeightAthlete(athleteId = athlete.id) },
                label = stringResource(Res.string.main_weight_athlete_label),
                placeholder = stringResource(Res.string.main_weight_athlete_placeholder),
            )
        },
    )
}

@Composable
private fun SyncDialog(
    state: MainUiState,
    actions: MainScreenActions,
    openBackupLauncher: () -> Unit,
) {
    val dialog = state.syncDialog ?: return
    val pickFrom = rememberDatePicker(
        initialDateMillis = dialog.fromMillis,
        onDatePicked = actions::changeSyncDateFrom,
    )
    val pickTo = rememberDatePicker(
        initialDateMillis = dialog.toMillis,
        onDatePicked = actions::changeSyncDateTo,
    )

    AlertDialog(
        onDismissRequest = actions::dismissSyncDialog,
        title = {
            Text(
                text = stringResource(Res.string.main_sync_title),
                style = TypographyPalette.H4,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                DateRangeFields(
                    fromValue = TimeUtils.formatDate(dialog.fromMillis),
                    onFromClick = pickFrom,
                    toValue = TimeUtils.formatDate(dialog.toMillis),
                    onToClick = pickTo,
                    isError = dialog.error != null,
                    supportingText = dialog.error?.resolve(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
                    Button(
                        modifier = Modifier.weight(weight = 1f),
                        text = stringResource(Res.string.main_sync_export),
                        size = ButtonSize.SMALL,
                        enabled = !dialog.isBusy,
                        loading = dialog.isExporting,
                        debounceClick = actions::exportSyncBackup,
                    )
                    Button(
                        modifier = Modifier.weight(weight = 1f),
                        text = stringResource(Res.string.main_sync_import),
                        size = ButtonSize.SMALL,
                        enabled = !dialog.isBusy,
                        loading = dialog.isImporting,
                        buttonColors = ButtonColors.secondary(
                            enabledText = MaterialTheme.colorScheme.onSurface,
                            enabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledText = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBackground = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        debounceClick = openBackupLauncher,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = actions::dismissSyncDialog,
                enabled = !dialog.isBusy,
            ) {
                Text(text = stringResource(UikitRes.string.uikit_cancel))
            }
        },
    )
}

@Composable
private fun SyncNotice(
    notice: UiText,
    actions: MainScreenActions,
) {
    AlertDialog(
        onDismissRequest = actions::dismissSyncNotice,
        title = { Text(text = stringResource(Res.string.main_sync_notice_title)) },
        text = { Text(text = notice.resolve()) },
        confirmButton = {
            TextButton(onClick = actions::dismissSyncNotice) {
                Text(text = stringResource(UikitRes.string.uikit_ok))
            }
        },
    )
}

@Composable
private fun RecentTrainingsSection(
    state: MainUiState,
    actions: MainScreenActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        SectionHeader(
            title = stringResource(Res.string.main_recent_trainings),
            trailing = if (state.trainingsCount > 0) {
                { ShowAllAction(onClick = actions::navigateToTrainingsScreen) }
            } else {
                null
            },
        )
        when {
            state.recentTrainings.isNotEmpty() -> {
                state.recentTrainings.forEach { training ->
                    RecentTrainingCard(
                        item = training,
                        onClick = { actions.onTrainingClick(training.trainingId) },
                    )
                }
            }

            !state.isLoading -> EmptyRecentTrainings()
        }
    }
}

@Composable
private fun ShowAllAction(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = stringResource(Res.string.main_show_all),
            style = TypographyPalette.Body2Medium,
        )
    }
}

@Composable
private fun RecentTrainingCard(
    item: TrainingDashboardItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.M),
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(Spacing.XS)
                    .height(ACCENT_BAR_HEIGHT)
                    .clip(RoundedCornerShape(Spacing._2XS))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Spacing._2XS),
            ) {
                Text(
                    text = stringResource(item.typeResId),
                    style = TypographyPalette.H4,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.work,
                    style = TypographyPalette.Body2Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.athletes.isNotEmpty()) {
                    Text(
                        text = item.athletes.joinToString(separator = ", "),
                        style = TypographyPalette.Body3Regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
            ) {
                Text(
                    text = item.date,
                    style = TypographyPalette.Caption1Regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.ML),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyRecentTrainings() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.XS),
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(EMPTY_ICON_SIZE),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.main_empty_trainings_title),
                style = TypographyPalette.H4,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.main_empty_trainings_hint),
                style = TypographyPalette.Body3Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val ACCENT_BAR_HEIGHT = 48.dp
private val EMPTY_ICON_SIZE = 32.dp
private const val HEADER_SUBTITLE_ALPHA = 0.85f

@Preview
@Composable
private fun MainScreenPreview() {
    ComposePreviewWrapper {
        SANBAITeamTheme {
            MainScreenContent(
                state = MainUiState(
                    isLoading = false,
                    trainingsCount = 3,
                    recentTrainings = emptyList(),
                ),
                actions = MainScreenActions.empty(),
            )
        }
    }
}