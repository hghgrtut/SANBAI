package by.rowing.sanbaiteam.training.presentation.detail.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonMetric
import by.rowing.sanbaiteam.training.data.models.comparison.ComparisonTrend
import by.rowing.sanbaiteam.training.data.models.comparison.MetricComparison
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_detail_avg_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_compare_average
import by.rowing.sanbaiteam.training.resources.training_detail_compare_hide
import by.rowing.sanbaiteam.training.resources.training_detail_compare_show
import by.rowing.sanbaiteam.training.resources.training_detail_compare_with_training
import by.rowing.sanbaiteam.training.resources.training_detail_delta_meters
import by.rowing.sanbaiteam.training.resources.training_detail_delta_pace
import by.rowing.sanbaiteam.training.resources.training_detail_delta_percent_points
import by.rowing.sanbaiteam.training.resources.training_detail_delta_seconds
import by.rowing.sanbaiteam.training.resources.training_detail_distance
import by.rowing.sanbaiteam.training.resources.training_detail_distance_label
import by.rowing.sanbaiteam.training.resources.training_detail_lactate_label
import by.rowing.sanbaiteam.training.resources.training_detail_max_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_pace_average
import by.rowing.sanbaiteam.training.resources.training_detail_pace_label
import by.rowing.sanbaiteam.training.resources.training_detail_percent_of_pb_label
import by.rowing.sanbaiteam.training.resources.training_detail_piece_number
import by.rowing.sanbaiteam.training.resources.training_detail_recovery_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_stroke_rate
import by.rowing.sanbaiteam.training.resources.training_detail_stroke_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_time_label
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val MISSING_VALUE = "—"

private val CHART_ICON_SIZE = 20.dp

@Composable
internal fun TrainingPieceCard(
    piece: TrainingDetailPiece,
    expanded: Boolean,
    actions: TrainingDetailActions,
) {
    val comparison = piece.comparison
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing._2XS)
            .animateContentSize()
    ) {
        Text(
            text = stringResource(Res.string.training_detail_piece_number, piece.order),
            style = TypographyPalette.Body2Medium
        )
        if (expanded && comparison != null) {
            Text(
                modifier = Modifier.padding(top = Spacing._2XS),
                text = comparisonCaption(comparison.comparedTrainingsCount),
                style = TypographyPalette.Caption1Regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.padding(top = Spacing.XS),
            verticalArrangement = Arrangement.spacedBy(Spacing._2XS)
        ) {
            PieceMetricRows(
                piece = piece,
                expanded = expanded
            )
        }
        if (comparison != null || piece.chartMetrics.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if ((comparison?.comparedTrainingsCount ?: 0) > 0) {
                    TextButton(
                        onClick = { actions.onToggleComparison(piece.order) },
                        contentPadding = PaddingValues(
                            horizontal = 0.dp,
                            vertical = Spacing.XS
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (expanded) {
                                    Res.string.training_detail_compare_hide
                                } else {
                                    Res.string.training_detail_compare_show
                                }
                            ),
                            style = TypographyPalette.Body2Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TrainingChartMetric.entries
                    .filter { it in piece.chartMetrics }
                    .forEach { metric ->
                        IconButton(
                            onClick = {
                                actions.onChartClick(
                                    order = piece.order,
                                    metric = metric
                                )
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(size = CHART_ICON_SIZE),
                                imageVector = metric.icon,
                                contentDescription = stringResource(metric.description),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun PieceMetricRows(
    piece: TrainingDetailPiece,
    expanded: Boolean,
) {
    val metrics = piece.comparison?.metrics.orEmpty()
    DistanceMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
    TimeMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
    PaceMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
    StrokeRateMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
    HeartRateMetricRow(
        label = stringResource(Res.string.training_detail_avg_heart_rate_label),
        baseValue = piece.avgHeartRate,
        comparison = metrics[ComparisonMetric.AVG_HEART_RATE],
        expanded = expanded
    )
    HeartRateMetricRow(
        label = stringResource(Res.string.training_detail_max_heart_rate_label),
        baseValue = piece.maxHeartRate,
        comparison = metrics[ComparisonMetric.MAX_HEART_RATE],
        expanded = expanded
    )
    HeartRateMetricRow(
        label = stringResource(Res.string.training_detail_recovery_heart_rate_label),
        baseValue = piece.recoveryHeartRate,
        comparison = metrics[ComparisonMetric.RECOVERY_HEART_RATE],
        expanded = expanded
    )
    LactateMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
    PercentOfPbMetricRow(
        piece = piece,
        metrics = metrics,
        expanded = expanded
    )
}

@Composable
private fun DistanceMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.DISTANCE]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_distance_label),
        baseValue = stringResource(Res.string.training_detail_distance, piece.distanceMeters),
        averageValue = comparison?.let {
            stringResource(Res.string.training_detail_distance, it.average.roundToInt())
        },
        deltaValue = comparison?.delta?.let {
            stringResource(
                Res.string.training_detail_delta_meters,
                formatSignedInt(it.roundToInt())
            )
        },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun TimeMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.TIME]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_time_label),
        baseValue = piece.timeFormatted,
        averageValue = comparison?.let { RowingTimeFormat.formatDuration(it.average.roundToLong()) },
        deltaValue = comparison?.delta?.let {
            stringResource(
                Res.string.training_detail_delta_seconds,
                formatSignedSeconds(it.roundToLong())
            )
        },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun PaceMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.PACE]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_pace_label),
        baseValue = piece.paceFormatted.ifEmpty { MISSING_VALUE },
        averageValue = comparison?.let {
            stringResource(
                Res.string.training_detail_pace_average,
                RowingTimeFormat.formatDuration(it.average.roundToLong())
            )
        },
        deltaValue = comparison?.delta?.let {
            stringResource(
                Res.string.training_detail_delta_pace,
                formatSignedSeconds(it.roundToLong())
            )
        },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun StrokeRateMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.STROKE_RATE]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_stroke_rate_label),
        baseValue = stringResource(
            Res.string.training_detail_stroke_rate,
            piece.strokeRateFormatted
        ),
        averageValue = comparison?.let {
            stringResource(
                Res.string.training_detail_stroke_rate,
                formatTenths(it.average)
            )
        },
        deltaValue = comparison?.delta?.let { formatSignedTenths(it) },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun HeartRateMetricRow(
    label: String,
    baseValue: Int?,
    comparison: MetricComparison?,
    expanded: Boolean,
) {
    TrainingMetricRow(
        label = label,
        baseValue = baseValue?.toString() ?: MISSING_VALUE,
        averageValue = comparison?.average?.roundToInt()?.toString(),
        deltaValue = comparison?.delta?.let { formatSignedInt(it.roundToInt()) },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun LactateMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.LACTATE]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_lactate_label),
        baseValue = piece.lactate?.let { formatTenths(it) } ?: MISSING_VALUE,
        averageValue = comparison?.let { formatTenths(it.average) },
        deltaValue = comparison?.delta?.let { formatSignedTenths(it) },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun PercentOfPbMetricRow(
    piece: TrainingDetailPiece,
    metrics: Map<ComparisonMetric, MetricComparison>,
    expanded: Boolean,
) {
    val comparison = metrics[ComparisonMetric.PERCENT_OF_PB]
    TrainingMetricRow(
        label = stringResource(Res.string.training_detail_percent_of_pb_label),
        baseValue = piece.percentOfPbFormatted,
        averageValue = comparison?.let { PersonalBestPercent.format(it.average) },
        deltaValue = comparison?.delta?.let {
            stringResource(
                Res.string.training_detail_delta_percent_points,
                formatSignedTenths(it)
            )
        },
        trend = comparison?.trend ?: ComparisonTrend.NEUTRAL,
        compare = expanded
    )
}

@Composable
private fun TrainingMetricRow(
    label: String,
    baseValue: String,
    averageValue: String?,
    deltaValue: String?,
    trend: ComparisonTrend,
    compare: Boolean,
) {
    val deltaColor = comparisonTrendColor(trend)
    val value = if (compare && averageValue != null) {
        buildAnnotatedString {
            append(baseValue)
            append(" ~ ")
            append(averageValue)
            if (deltaValue != null) {
                append(" (")
                withStyle(SpanStyle(color = deltaColor)) {
                    append(deltaValue)
                }
                append(")")
            }
        }
    } else {
        AnnotatedString(baseValue)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            modifier = Modifier.weight(0.4f),
            text = label,
            style = TypographyPalette.Body2Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            modifier = Modifier.weight(0.6f),
            text = value,
            style = TypographyPaletteSp.Body2Regular,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun comparisonTrendColor(trend: ComparisonTrend): Color = when (trend) {
    ComparisonTrend.BETTER -> if (isSystemInDarkTheme()) {
        ColorPalette.Green50
    } else {
        ColorPalette.Green30
    }

    ComparisonTrend.WORSE -> MaterialTheme.colorScheme.error
    ComparisonTrend.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun comparisonCaption(count: Int): String = if (count == 1) {
    stringResource(Res.string.training_detail_compare_with_training)
} else {
    pluralStringResource(Res.plurals.training_detail_compare_average, count, count)
}