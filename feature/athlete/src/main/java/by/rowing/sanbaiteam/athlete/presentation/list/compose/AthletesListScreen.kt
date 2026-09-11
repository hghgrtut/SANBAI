package by.rowing.sanbaiteam.athlete.presentation.list.compose

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.R
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteListItem
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesUiState
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
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
internal fun AthleteListScreen(viewModel: AthletesViewModel) {
    AthleteListScreenContent(
        actions = viewModel,
        state = viewModel.state
    )
}

@Composable
private fun AthleteListScreenContent(
    actions: AthletesListActions,
    state: AthletesUiState
) {

    val filteredAthletes = remember(state.searchQuery, state.items) {
        state.items.filter { athlete -> athlete.name.contains(state.searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AthleteListTopBarWithSearch(
                actions = actions,
                state = state
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = actions::onAddAthleteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(Spacing.M)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add athlete"
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
            if (state.showFilters) {
                AthleteFilters(
                    modifier = Modifier.padding(Spacing.M)
                )
                SpacerS()
            }

            if (filteredAthletes.isEmpty()) {
                if (state.searchQuery.isNotEmpty()) {
                    NoSearchResultsState(searchQuery = state.searchQuery)
                } else {
                    EmptyAthletesState(onAddAthlete = actions::onAddAthleteClick)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
                    contentPadding = PaddingValues(vertical = Spacing.S)
                ) {
                    items(filteredAthletes, key = { it.id }) { athlete ->
                        AthleteListItem(
                            athlete = athlete,
                            onAthleteClick = { actions.onAthleteClick(athlete.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AthleteListTopBarWithSearch(
    actions: AthletesListActions,
    state: AthletesUiState
) {
    Column {
        TopAppBar(
            title = stringResource(R.string.athletes_list_title),
            actions = {
                IconButton(onClick = { actions.onShowFiltersChange(!state.showFilters) }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter athletes",
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
            placeholder = { Text(stringResource(id = R.string.athletes_search_placeholder)) },
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
                .padding(horizontal = Spacing.M, vertical = Spacing.S)
        )
    }
}

@Composable
private fun AthleteFilters(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.SM)
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
            .padding(Spacing.XL),
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
            text = stringResource(R.string.athletes_no_athletes),
            style = TypographyPaletteSp.H2.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        SpacerXS()
        if (searchQuery.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.athletes_search_no_results, searchQuery),
                style = TypographyPaletteSp.Body2Medium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyAthletesState(onAddAthlete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.People,
            contentDescription = "No athletes",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        SpacerL()
        Text(
            text = stringResource(R.string.athletes_no_athletes),
            style = TypographyPaletteSp.H4.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        )
        SpacerS()
        Text(
            text = stringResource(id = R.string.athletes_add_someone_to_team),
            style = TypographyPaletteSp.Body2Medium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
        SpacerXL()
        Button(
            text = stringResource(R.string.athletes_add_athlete),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null
                )
            },
            debounceClick = onAddAthlete
        )
    }
}