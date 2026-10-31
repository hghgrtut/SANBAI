package by.rowing.sanbaiteam.training.presentation.comparison

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.presentation.filter.compose.TrainingFiltersActions

internal interface TrainingCompareActions : NavigateBackAware, TrainingFiltersActions {
    fun onToggleSelection(selectedTrainingId: Long)
    fun onConfirm()
    fun onReset()
}