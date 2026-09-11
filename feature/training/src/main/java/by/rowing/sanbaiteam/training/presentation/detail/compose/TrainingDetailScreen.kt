package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.rowing.sanbaiteam.training.R
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.uikit.component.SimpleTopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp

@Composable
internal fun TrainingDetailScreen(viewModel: TrainingDetailViewModel) {
    TrainingDetailContent(
        state = viewModel.state,
        onBackClick = viewModel::onBackClick
    )
}

@Composable
private fun TrainingDetailContent(
    state: TrainingDetailState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.training_detail_title),
                onNavIconClick = onBackClick
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            state.notFound -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.M),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.training_detail_not_found),
                        style = TypographyPaletteSp.Body1Regular
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(all = Spacing.M),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M)
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(bottom = Spacing.XS),
                            text = stringResource(state.typeResId),
                            style = TypographyPalette.Body2Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.dateFormatted,
                            style = TypographyPaletteSp.H2
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.M)) {
                                Text(
                                    text = stringResource(R.string.training_detail_crew_header),
                                    style = TypographyPalette.H4
                                )
                                SpacerS()
                                state.athleteNames.forEach { name ->
                                    Text(
                                        text = name,
                                        style = TypographyPaletteSp.Body1Regular
                                    )
                                    SpacerXS()
                                }
                            }
                        }
                    }
                    items(
                        state.pieces,
                        key = { it.order }
                    ) { piece ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.M)) {
                                PieceResultRow(piece)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceResultRow(piece: TrainingDetailPiece) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing._2XS)
    ) {
        Text(
            text = stringResource(R.string.training_detail_piece_number, piece.order),
            style = TypographyPalette.Body2Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.training_detail_distance, piece.distanceMeters),
                style = TypographyPaletteSp.Body2Regular
            )
            Text(
                text = piece.timeFormatted,
                style = TypographyPaletteSp.Body2Regular
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = piece.paceFormatted.ifEmpty { "—" },
                style = TypographyPaletteSp.Body2Regular.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = stringResource(
                    R.string.training_detail_stroke_rate,
                    piece.strokeRateFormatted
                ),
                style = TypographyPaletteSp.Body2Regular.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        SpacerXS()
        Text(
            text = stringResource(
                R.string.training_detail_percent_of_pb,
                piece.percentOfPbFormatted
            ),
            style = TypographyPaletteSp.Body2Regular.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
