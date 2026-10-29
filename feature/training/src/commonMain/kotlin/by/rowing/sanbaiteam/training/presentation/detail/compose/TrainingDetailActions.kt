package by.rowing.sanbaiteam.training.presentation.detail.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric

internal interface TrainingDetailActions : NavigateBackAware {

    fun onCompareClick()

    fun onToggleComparison(order: Int)

    fun onClearComparison()

    fun onChartClick(
        order: Int,
        metric: TrainingChartMetric
    )
}