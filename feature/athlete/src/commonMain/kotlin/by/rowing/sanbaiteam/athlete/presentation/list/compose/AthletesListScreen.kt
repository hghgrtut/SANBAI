package by.rowing.sanbaiteam.athlete.presentation.list.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.presentation.common.AthleteListItem
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesUiState
import by.rowing.sanbaiteam.athlete.presentation.list.AthletesViewModel
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete
import by.rowing.sanbaiteam.athlete.resources.athletes_add_someone_to_team
import by.rowing.sanbaiteam.athlete.resources.athletes_filter_content_description
import by.rowing.sanbaiteam.athlete.resources.athletes_filters_coming_soon
import by.rowing.sanbaiteam.athlete.resources.athletes_filters_title
import by.rowing.sanbaiteam.athlete.resources.athletes_list_title
import by.rowing.sanbaiteam.athlete.resources.athletes_no_athletes
import by.rowing.sanbaiteam.athlete.resources.athletes_search_clear
import by.rowing.sanbaiteam.athlete.resources.athletes_search_content_description
import by.rowing.sanbaiteam.athlete.resources.athletes_search_empty_title
import by.rowing.sanbaiteam.athlete.resources.athletes_search_no_results
import by.rowing.sanbaiteam.athlete.resources.athletes_search_placeholder
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Skeleton
import by.rowing.sanbaiteam.uikit.component.TextField
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

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
                    contentDescription = stringResource(Res.string.athletes_add_athlete)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
            if (state.showFilters) {
                AthleteFilters(
                    modifier = Modifier.padding(Spacing.M)
                )
            }

            val contentState = when {
                state.isLoading -> AthleteListContentState.LOADING
                filteredAthletes.isEmpty() -> AthleteListContentState.EMPTY
                else -> AthleteListContentState.CONTENT
            }
            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = CONTENT_FADE_IN_MILLIS)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = CONTENT_FADE_OUT_MILLIS))
                },
                label = "athleteListContent"
            ) { targetState ->
                when (targetState) {
                    AthleteListContentState.LOADING -> AthleteListSkeleton()
                    AthleteListContentState.EMPTY -> EmptyAthletesContent(
                        state = state,
                        actions = actions
                    )

                    AthleteListContentState.CONTENT -> AthleteList(
                        athletes = filteredAthletes,
                        actions = actions
                    )
                }
            }
        }
    }
}

@Composable
private fun AthleteList(
    athletes: List<AthleteItemModel>,
    actions: AthletesListActions
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
        contentPadding = PaddingValues(vertical = Spacing.S)
    ) {
        items(athletes, key = { it.id }) { athlete ->
            AthleteListItem(
                modifier = Modifier.animateItem(),
                athlete = athlete,
                onAthleteClick = { actions.onAthleteClick(athlete.id) }
            )
        }
    }
}

@Composable
private fun AthleteListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Spacing.S),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
    ) {
        repeat(times = SKELETON_ITEM_COUNT) {
            AthleteSkeletonItem()
        }
    }
}

@Composable
private fun AthleteSkeletonItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.M),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Skeleton(
                modifier = Modifier.size(SKELETON_AVATAR_SIZE),
                shape = CircleShape,
            )
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.S),
            ) {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = SKELETON_NAME_FRACTION)
                        .height(SKELETON_NAME_HEIGHT)
                )
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = SKELETON_BADGE_FRACTION)
                        .height(SKELETON_BADGE_HEIGHT)
                )
            }
        }
    }
}

@Composable
private fun EmptyAthletesContent(
    state: AthletesUiState,
    actions: AthletesListActions
) {
    if (state.searchQuery.isNotEmpty()) {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.athletes_search_empty_title),
            description = stringResource(Res.string.athletes_search_no_results, state.searchQuery),
            icon = Icons.Default.SearchOff,
        )
    } else {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.athletes_no_athletes),
            description = stringResource(Res.string.athletes_add_someone_to_team),
            icon = Icons.Default.People,
            actionText = stringResource(Res.string.athletes_add_athlete),
            onActionClick = actions::onAddAthleteClick,
        )
    }
}

@Composable
private fun AthleteListTopBarWithSearch(
    actions: AthletesListActions,
    state: AthletesUiState
) {
    Column {
        TopAppBar(
            title = stringResource(Res.string.athletes_list_title),
            actions = {
                IconButton(onClick = { actions.onShowFiltersChange(!state.showFilters) }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(Res.string.athletes_filter_content_description),
                        tint = if (state.showFilters) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            onNavIconClick = actions::onBackClick
        )
        TextField(
            value = state.searchQuery,
            onValueChange = actions::onSearchQueryChange,
            placeholder = { Text(stringResource(Res.string.athletes_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.athletes_search_content_description)
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.athletes_search_clear)
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
private fun AthleteFilters(modifier: Modifier = Modifier) {
    DataCard(
        modifier = modifier,
        verticalSpacing = Spacing.SM
    ) {
        Text(
            text = stringResource(Res.string.athletes_filters_title),
            style = TypographyPalette.H4,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.athletes_filters_coming_soon),
            style = TypographyPalette.Body2Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class AthleteListContentState {
    LOADING,
    EMPTY,
    CONTENT,
}

private const val CONTENT_FADE_IN_MILLIS = 220
private const val CONTENT_FADE_OUT_MILLIS = 140

private val SKELETON_AVATAR_SIZE = 60.dp
private val SKELETON_NAME_HEIGHT = 20.dp
private val SKELETON_BADGE_HEIGHT = 16.dp
private const val SKELETON_NAME_FRACTION = 0.5f
private const val SKELETON_BADGE_FRACTION = 0.3f
private const val SKELETON_ITEM_COUNT = 6
