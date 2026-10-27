package by.rowing.sanbaiteam.training.presentation.detail.compose

import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric

internal interface TrainingDetailActions {

    fun onBackClick()

    fun onCompareClick()

    fun onToggleComparison(order: Int)

    fun onClearComparison()

    fun onChartClick(
        order: Int,
        metric: TrainingChartMetric
    )
}