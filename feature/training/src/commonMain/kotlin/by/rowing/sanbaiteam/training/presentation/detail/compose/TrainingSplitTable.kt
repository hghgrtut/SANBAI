package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.training.data.chart.models.TrainingPieceSplit
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_split_column_avg_heart_rate
import by.rowing.sanbaiteam.training.resources.training_split_column_distance
import by.rowing.sanbaiteam.training.resources.training_split_column_max_heart_rate
import by.rowing.sanbaiteam.training.resources.training_split_column_number
import by.rowing.sanbaiteam.training.resources.training_split_column_pace
import by.rowing.sanbaiteam.training.resources.training_split_column_spm
import by.rowing.sanbaiteam.training.resources.training_split_column_time
import by.rowing.sanbaiteam.training.resources.training_split_total
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private const val MISSING_VALUE = "—"
private val LAZY_TABLE_MAX_HEIGHT = 420.dp

private val NUMBER_WIDTH = 26.dp
private val DISTANCE_WIDTH = 56.dp
private val TIME_WIDTH = 72.dp
private val PACE_WIDTH = 76.dp
private val SPM_WIDTH = 56.dp
private val AVG_HEART_RATE_WIDTH = 56.dp
private val MAX_HEART_RATE_WIDTH = 56.dp
private val TABLE_WIDTH = NUMBER_WIDTH + DISTANCE_WIDTH + TIME_WIDTH + PACE_WIDTH +
        SPM_WIDTH + AVG_HEART_RATE_WIDTH + MAX_HEART_RATE_WIDTH

@Composable
internal fun TrainingSplitTable(
    piece: TrainingDetailPiece,
    splits: List<TrainingPieceSplit>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column(modifier = Modifier.width(TABLE_WIDTH)) {
            SplitTableHeader()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(modifier = Modifier.heightIn(max = LAZY_TABLE_MAX_HEIGHT)) {
                items(
                    items = splits,
                    key = { split -> split.index }
                ) { split ->
                    SplitTableRowLayout(
                        index = split.index.toString(),
                        distance = split.distanceMeters.toString(),
                        time = RowingTimeFormat.formatDuration(split.timeMillis),
                        pace = split.paceMillis?.let { RowingTimeFormat.formatDuration(it) } ?: MISSING_VALUE,
                        strokeRate = split.strokeRate?.let { formatTenths(it) } ?: MISSING_VALUE,
                        avgHeartRate = split.avgHeartRate?.toString() ?: MISSING_VALUE,
                        maxHeartRate = split.maxHeartRate?.toString() ?: MISSING_VALUE,
                        style = TypographyPalette.Body2Regular,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SplitTotalRow(piece = piece)
        }
    }
}

@Composable
private fun SplitTableHeader() {
    SplitTableRowLayout(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
        index = stringResource(Res.string.training_split_column_number),
        distance = stringResource(Res.string.training_split_column_distance),
        time = stringResource(Res.string.training_split_column_time),
        pace = stringResource(Res.string.training_split_column_pace),
        strokeRate = stringResource(Res.string.training_split_column_spm),
        avgHeartRate = stringResource(Res.string.training_split_column_avg_heart_rate),
        maxHeartRate = stringResource(Res.string.training_split_column_max_heart_rate),
        style = TypographyPalette.Caption1Medium,
    )
}

@Composable
private fun SplitTotalRow(piece: TrainingDetailPiece) {
    SplitTableRowLayout(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
        index = stringResource(Res.string.training_split_total),
        distance = piece.distanceMeters.toString(),
        time = piece.timeFormatted.ifEmpty { MISSING_VALUE },
        pace = RowingTimeFormat.paceMillis(
            timeMillis = piece.timeMillis,
            distanceMeters = piece.distanceMeters,
        )?.let { RowingTimeFormat.formatDuration(it) } ?: MISSING_VALUE,
        strokeRate = piece.strokeRateFormatted.ifEmpty { MISSING_VALUE },
        avgHeartRate = piece.avgHeartRate?.toString() ?: MISSING_VALUE,
        maxHeartRate = piece.maxHeartRate?.toString() ?: MISSING_VALUE,
        style = TypographyPalette.Body2Medium,
    )
}

@Composable
private fun SplitTableRowLayout(
    index: String,
    distance: String,
    time: String,
    pace: String,
    strokeRate: String,
    avgHeartRate: String,
    maxHeartRate: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        TableCell(
            text = index,
            width = NUMBER_WIDTH,
            align = TextAlign.Start,
            style = style,
        )
        TableCell(
            text = distance,
            width = DISTANCE_WIDTH,
            style = style
        )
        TableCell(
            text = time,
            width = TIME_WIDTH,
            style = style
        )
        TableCell(
            text = pace,
            width = PACE_WIDTH,
            style = style
        )
        TableCell(
            text = strokeRate,
            width = SPM_WIDTH,
            style = style
        )
        TableCell(
            text = avgHeartRate,
            width = AVG_HEART_RATE_WIDTH,
            style = style
        )
        TableCell(
            text = maxHeartRate,
            width = MAX_HEART_RATE_WIDTH,
            style = style
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    style: TextStyle,
    align: TextAlign = TextAlign.End,
) {
    Text(
        modifier = Modifier
            .width(width)
            .padding(all = Spacing._2XS),
        text = text,
        style = style,
        textAlign = align,
        maxLines = 1,
    )
}