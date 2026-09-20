package by.rowing.sanbaiteam.training.presentation.list.compose

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import by.rowing.sanbaiteam.athlete.presentation.common.ItemCard
import by.rowing.sanbaiteam.training.R
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val JSON_APP_MIME_TYPE = "application/json"

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
    val context = LocalContext.current
    val filteredTrainings = remember(state.searchQuery, state.items) {
        state.items.filter { training ->
            training.athletes.any { it.contains(state.searchQuery, ignoreCase = true) }
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = JSON_APP_MIME_TYPE)
    ) { uri ->
        actions.onExportToUri(
            uri = uri ?: return@rememberLauncherForActivityResult,
            context = context
        )
    }
    val openBackupLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
        actions.onImportFromUri(
            uri = it ?: return@rememberLauncherForActivityResult,
            context = context
        )
    }

    LaunchedEffect(state.exportPromptRequested) {
        if (state.exportPromptRequested) {
            createBackupLauncher.launch(suggestedBackupFileName())
            actions.onExportPromptLaunched()
        }
    }

    BackHandler { actions.onBackClick() }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ListTrainingTopBarWithSearch(
                actions = actions,
                openBackupLauncher = openBackupLauncher,
                state = state
            )
        },
        floatingActionButton = { AddTrainingFAB(onAddTraining = actions::onAddTrainingClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues = paddingValues),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.S)
        ) {
            if (state.showFilters) {
                TrainingFilters()
            }
            if (filteredTrainings.isEmpty()) {
                EmptyTrainingList(
                    state = state,
                    actions = actions
                )
            } else {
                TrainingList(
                    trainings = filteredTrainings,
                    actions = actions
                )
            }
        }
    }
    state.transferNotice?.let { notice ->
        TransferDataAlert(
            actions = actions,
            notice = notice
        )
    }
}

@Composable
private fun ListTrainingTopBarWithSearch(
    state: ListTrainingState,
    openBackupLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    actions: ListTrainingScreenActions
) {
    Column {
        TopAppBar(
            title = stringResource(R.string.training_list_title),
            actions = {
                IconButton(
                    onClick = actions::onExportClick,
                    enabled = !state.isTransferring
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = stringResource(R.string.training_transfer_export_action)
                    )
                }
                IconButton(
                    onClick = {
                        openBackupLauncher.launch(arrayOf(JSON_APP_MIME_TYPE, "text/json", "text/plain"))
                    },
                    enabled = !state.isTransferring
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = stringResource(R.string.training_transfer_import_action)
                    )
                }
                IconButton(onClick = { actions.onShowFiltersChange(!state.showFilters) }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter trainings",
                        tint = if (state.showFilters) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            },
            onNavIconClick = actions::onBackClick
        )
        TextField(
            value = state.searchQuery,
            onValueChange = actions::onSearchQueryChange,
            placeholder = { Text(stringResource(id = R.string.training_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.M,
                    vertical = Spacing.S
                )
        )
    }
}

@Composable
private fun TrainingFilters() {
    DataCard(
        modifier = Modifier.padding(all = Spacing.M),
        verticalSpacing = Spacing.SM
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Filter options coming soon...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoSearchResultsState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "No results",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SpacerL()
        Text(
            text = stringResource(R.string.training_no_trainings),
            style = TypographyPaletteSp.H2.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        SpacerXS()
        if (searchQuery.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.training_search_no_results, searchQuery),
                style = TypographyPaletteSp.Body2Medium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyTrainingsState(onAddAthlete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TableRows,
            contentDescription = "Нет тренировок",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        SpacerL()
        Text(
            text = stringResource(R.string.training_no_trainings),
            style = TypographyPaletteSp.H4.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        )
        SpacerS()
        Text(
            text = stringResource(id = R.string.training_add_some_training),
            style = TypographyPaletteSp.Body2Medium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
        SpacerXL()
        Button(
            text = stringResource(R.string.training_add_training),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null
                )
            },
            debounceClick = onAddAthlete
        )
    }
}

@Composable
private fun ListTrainingItem(
    item: ListTrainingStateItem,
    onTrainingClick: (ListTrainingStateItem) -> Unit
) {
    ItemCard(onClick = { onTrainingClick(item) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.S)
        ) {
            Text(
                text = item.trainingWork,
                style = TypographyPaletteSp.H1.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Text(
                text = item.athletes.joinToString(),
                style = TypographyPaletteSp.H3.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Text(
                text = item.trainingDate,
                style = TypographyPaletteSp.Body2Regular.copy(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
private fun TrainingList(
    trainings: List<ListTrainingStateItem>,
    actions: ListTrainingScreenActions
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
        contentPadding = PaddingValues(vertical = Spacing.S)
    ) {
        items(trainings, key = { it.trainingId }) { training ->
            ListTrainingItem(
                item = training,
                onTrainingClick = { actions.onTrainingClick(training.trainingId) }
            )
        }
    }
}

@Composable
private fun EmptyTrainingList(
    state: ListTrainingState,
    actions: ListTrainingScreenActions
) {
    if (state.searchQuery.isNotEmpty()) {
        NoSearchResultsState(searchQuery = state.searchQuery)
    } else {
        EmptyTrainingsState(onAddAthlete = actions::onAddTrainingClick)
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
            contentDescription = stringResource(R.string.training_add_training)
        )
    }
}

@Composable
private fun TransferDataAlert(
    actions: ListTrainingScreenActions,
    notice: String
) {
    AlertDialog(
        onDismissRequest = actions::onDismissTransferNotice,
        title = { Text(text = stringResource(id = R.string.training_transfer_notice_title)) },
        text = { Text(text = notice) },
        confirmButton = {
            TextButton(onClick = actions::onDismissTransferNotice) { Text(text = stringResource(id = R.string.ok)) }
        }
    )
}

private fun suggestedBackupFileName(): String =
    "sanbai-training-backup-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + ".json"