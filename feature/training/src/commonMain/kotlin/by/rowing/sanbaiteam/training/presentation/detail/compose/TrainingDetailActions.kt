package by.rowing.sanbaiteam.training.presentation.detail.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.data.chart.models.TrainingSplitLength
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.detail.TrainingDetailField

internal interface TrainingDetailActions : NavigateBackAware {

    fun onCompareClick()

    fun onToggleComparison(order: Int)

    fun onClearComparison()

    fun onChartClick(
        order: Int,
        metric: TrainingChartMetric
    )

    fun onSettingsClick()

    fun onSettingsDismiss()

    fun onFieldToggle(field: TrainingDetailField)

    fun onShowAllFields()

    fun onSplitApply(length: TrainingSplitLength)

    fun onSplitClear()
}