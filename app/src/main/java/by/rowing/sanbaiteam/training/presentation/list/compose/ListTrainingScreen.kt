package by.rowing.sanbaiteam.training.presentation.list.compose

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.athlete.presentation.common.ItemCard
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp

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
    val filteredTrainings = remember(state.searchQuery, state.items) {
        state.items.filter { training ->
            training.athletes.any { it.contains(state.searchQuery, ignoreCase = true) }
        }
    }

    BackHandler { actions.onBackClick() }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ListTrainingTopBarWithSearch(
                actions = actions,
                state = state
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = actions::onAddTrainingClick,
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
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
            if (state.showFilters) {
                TrainingFilters()
                SpacerS()
            }

            if (filteredTrainings.isEmpty()) {
                if (state.searchQuery.isNotEmpty()) {
                    NoSearchResultsState(searchQuery = state.searchQuery)
                } else {
                    EmptyTrainingsState(onAddAthlete = actions::onAddTrainingClick)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
                    contentPadding = PaddingValues(vertical = Spacing.S)
                ) {
                    items(filteredTrainings, key = { it.trainingId }) { training ->
                        ListTrainingItem(
                            item = training,
                            onTrainingClick = { actions.onTrainingClick(training.trainingId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListTrainingTopBarWithSearch(
    state: ListTrainingState,
    actions: ListTrainingScreenActions
) {
    Column {
        TopAppBar(
            title = stringResource(R.string.training_list_title),
            actions = {
                IconButton(onClick = { actions.onShowFiltersChange(!state.showFilters) }) {
                    Icon(
                        Icons.Default.FilterList,
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
    Card(
        modifier = Modifier.padding(all = Spacing.M),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
    ) {
        Column(
            modifier = Modifier.padding(all = Spacing.M),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.SM)
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