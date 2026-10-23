package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_detail_compare_action
import by.rowing.sanbaiteam.training.resources.training_detail_compare_clear
import by.rowing.sanbaiteam.training.resources.training_detail_compare_count
import by.rowing.sanbaiteam.training.resources.training_detail_crew_header
import by.rowing.sanbaiteam.training.resources.training_detail_not_found
import by.rowing.sanbaiteam.training.resources.training_detail_title
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Skeleton
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val SKELETON_CAPTION_HEIGHT = 16.dp
private val SKELETON_DATE_HEIGHT = 28.dp
private val SKELETON_CARD_HEIGHT = 96.dp
private const val SKELETON_TITLE_FRACTION = 0.3f
private const val SKELETON_DATE_FRACTION = 0.55f
private const val SKELETON_CARD_COUNT = 3

@Composable
internal fun TrainingDetailScreen(viewModel: TrainingDetailViewModel) {
    TrainingDetailContent(
        state = viewModel.state,
        actions = viewModel,
    )
}

@Composable
private fun TrainingDetailContent(
    state: TrainingDetailState,
    actions: TrainingDetailActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.training_detail_title),
                onNavIconClick = actions::onBackClick,
                actions = {
                    if (!state.isLoading && !state.notFound) {
                        IconButton(onClick = actions::onCompareClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = stringResource(Res.string.training_detail_compare_action),
                                tint = if (state.comparedTrainingIds.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> TrainingLoader(paddingValues = paddingValues)
            state.notFound -> TrainingNotFound(paddingValues = paddingValues)
            else -> Training(
                paddingValues = paddingValues,
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun TrainingLoader(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth(fraction = SKELETON_TITLE_FRACTION)
                    .height(SKELETON_CAPTION_HEIGHT)
            )
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth(fraction = SKELETON_DATE_FRACTION)
                    .height(SKELETON_DATE_HEIGHT)
            )
        }
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

@Composable
private fun TrainingNotFound(paddingValues: PaddingValues) {
    EmptyState(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        title = stringResource(Res.string.training_detail_not_found),
        icon = Icons.Default.SearchOff,
    )
}

@Composable
private fun Training(
    paddingValues: PaddingValues,
    state: TrainingDetailState,
    actions: TrainingDetailActions,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M)
    ) {
        item {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = Spacing.XS),
                    text = state.typeResId?.let { stringResource(it) }.orEmpty(),
                    style = TypographyPalette.Body2Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.dateFormatted,
                    style = TypographyPaletteSp.H2
                )
                if (state.comparedTrainingIds.isNotEmpty()) {
                    ComparisonChip(
                        count = state.comparedTrainingIds.size,
                        onClick = actions::onCompareClick,
                        onClear = actions::onClearComparison,
                    )
                }
            }
        }
        item {
            DataCard(
                modifier = Modifier.fillMaxWidth(),
                verticalSpacing = 0.dp
            ) {
                Text(
                    modifier = Modifier.padding(bottom = Spacing.S),
                    text = stringResource(Res.string.training_detail_crew_header),
                    style = TypographyPalette.H4
                )
                state.athleteNames.forEach { name ->
                    Text(
                        modifier = Modifier.padding(bottom = Spacing.XS),
                        text = name,
                        style = TypographyPaletteSp.Body1Regular
                    )
                }
            }
        }
        items(
            state.pieces,
            key = { it.order }
        ) { piece ->
            DataCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                verticalSpacing = 0.dp
            ) {
                TrainingPieceCard(
                    piece = piece,
                    expanded = piece.order in state.expandedComparisonOrders,
                    onToggleComparison = { actions.onToggleComparison(piece.order) },
                )
            }
        }
    }
}

@Composable
private fun ComparisonChip(
    count: Int,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        modifier = Modifier.padding(top = Spacing.S),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(start = Spacing.XS)
                .padding(all = Spacing.XS),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.XS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
            )
            Text(
                text = pluralStringResource(
                    Res.plurals.training_detail_compare_count,
                    count,
                    count,
                ),
                style = TypographyPalette.Caption1Medium,
            )
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = onClear,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.training_detail_compare_clear),
                )
            }
        }
    }
}
