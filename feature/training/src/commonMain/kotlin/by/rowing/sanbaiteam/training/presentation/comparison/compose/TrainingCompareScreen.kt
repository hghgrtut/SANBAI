package by.rowing.sanbaiteam.training.presentation.comparison.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.presentation.common.ItemCard
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingCompareActions
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingCompareViewModel
import by.rowing.sanbaiteam.training.presentation.comparison.models.TrainingCompareState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_compare_confirm
import by.rowing.sanbaiteam.training.resources.training_compare_empty_description
import by.rowing.sanbaiteam.training.resources.training_compare_empty_title
import by.rowing.sanbaiteam.training.resources.training_compare_reset
import by.rowing.sanbaiteam.training.resources.training_compare_subtitle
import by.rowing.sanbaiteam.training.resources.training_compare_title
import by.rowing.sanbaiteam.uikit.component.Checkbox
import by.rowing.sanbaiteam.uikit.component.CheckedState
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Skeleton
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.button.Button
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private val SKELETON_CARD_HEIGHT = 96.dp
private const val SKELETON_CARD_COUNT = 3

@Composable
internal fun TrainingCompareScreen(viewModel: TrainingCompareViewModel) {
    TrainingCompareContent(
        state = viewModel.state,
        actions = viewModel,
    )
}

@Composable
private fun TrainingCompareContent(
    state: TrainingCompareState,
    actions: TrainingCompareActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.training_compare_title),
                onNavIconClick = actions::navigateBack,
            )
        },
        bottomBar = {
            if (!state.isLoading && state.items.isNotEmpty() && state.selectedIds.isNotEmpty()) {
                CompareBottomBar(
                    selectedCount = state.selectedIds.size,
                    onConfirm = actions::onConfirm,
                )
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> CompareLoader(paddingValues = paddingValues)
            state.items.isEmpty() -> EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                title = stringResource(Res.string.training_compare_empty_title),
                description = stringResource(Res.string.training_compare_empty_description),
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                actionText = stringResource(Res.string.training_compare_reset).takeIf { state.hasAppliedComparison },
                onActionClick = actions::onReset.takeIf { state.hasAppliedComparison }
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = Spacing.S),
            ) {
                item {
                    CompareHeader(
                        state = state,
                        onReset = actions::onReset,
                    )
                }
                items(
                    items = state.items,
                    key = { it.trainingId }
                ) { item ->
                    CompareTrainingItem(
                        item = item,
                        selected = item.trainingId in state.selectedIds,
                        onToggle = actions::onToggleSelection,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareHeader(
    state: TrainingCompareState,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
    ) {
        state.typeResId?.let { typeResId ->
            Text(
                text = stringResource(
                    Res.string.training_compare_subtitle,
                    stringResource(typeResId),
                ),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.hasAppliedComparison) {
            TextButton(onClick = onReset) {
                Text(
                    text = stringResource(Res.string.training_compare_reset),
                    style = TypographyPalette.Body2Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CompareTrainingItem(
    item: ListTrainingStateItem,
    selected: Boolean,
    onToggle: (Long) -> Unit,
) {
    ItemCard(
        onClick = { onToggle(item.trainingId) },
        selected = selected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
            ) {
                Text(
                    text = item.trainingDate,
                    style = TypographyPalette.Caption1Regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            Checkbox(
                checked = if (selected) CheckedState.CHECKED else CheckedState.UNCHECKED,
                click = { onToggle(item.trainingId) },
            )
        }
    }
}

@Composable
private fun CompareBottomBar(
    selectedCount: Int,
    onConfirm: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(Spacing.M),
            text = stringResource(Res.string.training_compare_confirm, selectedCount),
            debounceClick = onConfirm,
        )
    }
}

@Composable
private fun CompareLoader(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        repeat(times = SKELETON_CARD_COUNT) {
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SKELETON_CARD_HEIGHT),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}