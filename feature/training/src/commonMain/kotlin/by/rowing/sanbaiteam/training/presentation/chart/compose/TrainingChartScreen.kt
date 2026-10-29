package by.rowing.sanbaiteam.training.presentation.chart.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartPoint
import by.rowing.sanbaiteam.training.data.models.chart.TrainingPieceChartData
import by.rowing.sanbaiteam.training.presentation.chart.TrainingChartActions
import by.rowing.sanbaiteam.training.presentation.chart.TrainingChartViewModel
import by.rowing.sanbaiteam.training.presentation.chart.models.TrainingChartState
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_chart_average
import by.rowing.sanbaiteam.training.resources.training_chart_best
import by.rowing.sanbaiteam.training.resources.training_chart_empty_metric
import by.rowing.sanbaiteam.training.resources.training_chart_faster
import by.rowing.sanbaiteam.training.resources.training_chart_heart_rate_value
import by.rowing.sanbaiteam.training.resources.training_chart_max
import by.rowing.sanbaiteam.training.resources.training_chart_pace_value
import by.rowing.sanbaiteam.training.resources.training_chart_piece_not_found
import by.rowing.sanbaiteam.training.resources.training_chart_source_unavailable
import by.rowing.sanbaiteam.training.resources.training_chart_stroke_rate_value
import by.rowing.sanbaiteam.training.resources.training_chart_title
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Throbber
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.chart.MetricLineChart
import by.rowing.sanbaiteam.uikit.component.chart.models.MetricChartPoint
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
internal fun TrainingChartScreen(viewModel: TrainingChartViewModel) {
    TrainingChartContent(
        state = viewModel.state,
        actions = viewModel,
    )
}

@Composable
private fun TrainingChartContent(
    state: TrainingChartState,
    actions: TrainingChartActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.training_chart_title, state.order),
                onNavIconClick = actions::navigateBack,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.M),
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { Throbber() }

                state.pieceNotFound -> EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(Res.string.training_chart_piece_not_found),
                    icon = Icons.Filled.SearchOff,
                )

                state.sourceUnavailable -> EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(Res.string.training_chart_source_unavailable),
                    icon = Icons.Filled.SearchOff,
                )

                state.chartData == null || state.availableMetrics.isEmpty() -> EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(Res.string.training_chart_empty_metric),
                    icon = Icons.Filled.SearchOff,
                )

                else -> ChartBody(
                    state = state,
                    data = state.chartData,
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun ChartBody(
    state: TrainingChartState,
    data: TrainingPieceChartData,
    actions: TrainingChartActions,
) {
    val selectedMetric = state.selectedMetric ?: return
    val points = data.points(selectedMetric)
    if (points.isEmpty()) {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.training_chart_empty_metric),
            icon = Icons.Filled.SearchOff,
        )
        return
    }
    val contentDescription = stringResource(selectedMetric.description) + ". " +
            stringResource(Res.string.training_chart_average) + " " + summaryValues(selectedMetric, points).first

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        MetricSelector(
            availableMetrics = state.availableMetrics,
            selectedMetric = selectedMetric,
            onMetricSelected = actions::onMetricSelected,
        )
        SummaryRow(
            metric = selectedMetric,
            points = points,
        )
        DataCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalSpacing = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MetricLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .semantics { this.contentDescription = contentDescription },
                    points = points.map {
                        MetricChartPoint(
                            elapsedMillis = it.elapsedMillis,
                            value = it.value
                        )
                    },
                    durationMillis = data.durationMillis,
                    averageValue = points.map { it.value }.average(),
                    reverseValueAxis = selectedMetric == TrainingChartMetric.SPEED,
                    valueFormatter = { value ->
                        formatAxisValue(
                            metric = selectedMetric,
                            value = value
                        )
                    },
                    timeFormatter = RowingTimeFormat::formatDurationMmSs
                )
                if (selectedMetric == TrainingChartMetric.SPEED) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(
                                top = Spacing.XS,
                                end = Spacing.XS
                            ),
                        text = stringResource(Res.string.training_chart_faster),
                        style = TypographyPalette.Caption1Regular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSelector(
    availableMetrics: List<TrainingChartMetric>,
    selectedMetric: TrainingChartMetric,
    onMetricSelected: (TrainingChartMetric) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
    ) {
        availableMetrics.forEach { metric ->
            FilterChip(
                selected = metric == selectedMetric,
                onClick = { onMetricSelected(metric) },
                label = {
                    Text(
                        text = stringResource(metric.description),
                        style = TypographyPalette.Body2Medium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = metric.icon,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun SummaryRow(
    metric: TrainingChartMetric,
    points: List<TrainingChartPoint>,
) {
    val (average, extreme) = summaryValues(
        metric = metric,
        points = points
    )
    val maximumLabelResId =
        if (metric == TrainingChartMetric.SPEED) Res.string.training_chart_best else Res.string.training_chart_max
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        SummaryItem(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.training_chart_average),
            value = average,
        )
        SummaryItem(
            modifier = Modifier.weight(1f),
            label = stringResource(maximumLabelResId),
            value = extreme,
        )
    }
}

@Composable
private fun SummaryItem(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TypographyPalette.Caption1Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = TypographyPalette.H4,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Среднее значение to максимальное значение */
@Composable
private fun summaryValues(
    metric: TrainingChartMetric,
    points: List<TrainingChartPoint>,
): Pair<String, String> = formatSummaryValue(
    metric = metric,
    value = points.map { it.value }.average()
) to formatSummaryValue(
    metric = metric,
    value = if (metric == TrainingChartMetric.SPEED) points.minOf { it.value } else points.maxOf { it.value }
)

@Composable
private fun formatSummaryValue(
    metric: TrainingChartMetric,
    value: Double,
): String = when (metric) {
    TrainingChartMetric.SPEED -> stringResource(
        Res.string.training_chart_pace_value,
        RowingTimeFormat.formatDuration((value * TimeUtils.MILLIS_PER_SECOND).roundToLong()),
    )

    TrainingChartMetric.STROKE_RATE -> stringResource(
        Res.string.training_chart_stroke_rate_value,
        formatTenths(value),
    )

    TrainingChartMetric.HEART_RATE -> stringResource(
        Res.string.training_chart_heart_rate_value,
        value.roundToInt(),
    )
}

private fun formatAxisValue(
    metric: TrainingChartMetric,
    value: Double,
): String = when (metric) {
    TrainingChartMetric.SPEED ->
        RowingTimeFormat.formatDurationMmSs(timeMillis = (value * TimeUtils.MILLIS_PER_SECOND).roundToLong())

    TrainingChartMetric.STROKE_RATE -> value.roundToInt().toString()
    TrainingChartMetric.HEART_RATE -> value.roundToInt().toString()
}