package by.rowing.sanbaiteam.training.presentation.chart

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric

internal interface TrainingChartActions : NavigateBackAware {

    fun onMetricSelected(metric: TrainingChartMetric)
}