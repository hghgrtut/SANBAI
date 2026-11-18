package by.rowing.sanbaiteam.athlete.presentation.chart.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.athlete.presentation.chart.AthleteWeightChartState
import by.rowing.sanbaiteam.athlete.presentation.chart.AthleteWeightChartViewModel
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_not_found
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_chart_average
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_chart_empty
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_chart_title
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_empty_value
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_value_kg
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.EmptyState
import by.rowing.sanbaiteam.uikit.component.Throbber
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.component.chart.MetricLineChart
import by.rowing.sanbaiteam.uikit.modifier.scaffoldContentInsets
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private const val TENTHS_IN_KILOGRAM = 10.0

@Composable
internal fun AthleteWeightChartScreen(viewModel: AthleteWeightChartViewModel) {
    AthleteWeightChartContent(
        state = viewModel.state,
        navigateBack = viewModel::navigateBack,
    )
}

@Composable
private fun AthleteWeightChartContent(
    state: AthleteWeightChartState,
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.athletes_weight_chart_title),
                onNavIconClick = navigateBack,
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { Throbber() }

            state.notFound -> EmptyState(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(Res.string.athletes_detail_not_found),
                icon = Icons.Filled.SearchOff,
            )

            state.points.isEmpty() -> EmptyState(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(Res.string.athletes_weight_chart_empty),
                icon = Icons.Filled.SearchOff,
            )

            else -> ChartBody(
                state = state,
                paddingValues = paddingValues,
            )
        }
    }
}

@Composable
private fun ChartBody(
    state: AthleteWeightChartState,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .scaffoldContentInsets(paddingValues)
            .fillMaxSize()
            .padding(horizontal = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        AverageCard(averageWeightTenths = state.averageWeightTenths)
        DataCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
                .padding(bottom = Spacing.M),
            verticalSpacing = Spacing.None,
        ) {
            MetricLineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f),
                points = state.points,
                durationMillis = state.durationMillis.coerceAtLeast(minimumValue = 1L),
                averageValue = state.averageWeightTenths?.div(TENTHS_IN_KILOGRAM),
                valueFormatter = { value -> formatTenths(value = value) },
                timeFormatter = { offset -> TimeUtils.formatDayMonth(timeInMillis = state.fromMillis + offset) },
                timeTicks = state.timeTicks,
                connectAllPoints = true,
            )
        }
    }
}

@Composable
private fun AverageCard(averageWeightTenths: Int?) {
    DataCard(
        modifier = Modifier.fillMaxWidth(),
        verticalSpacing = Spacing.None,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        ) {
            Text(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(Res.string.athletes_weight_chart_average),
                style = TypographyPalette.Body2Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = averageWeightTenths
                    ?.let { stringResource(Res.string.athletes_weight_value_kg, formatTenths(value = it / TENTHS_IN_KILOGRAM)) }
                    ?: stringResource(Res.string.athletes_weight_empty_value),
                style = TypographyPalette.H4,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}