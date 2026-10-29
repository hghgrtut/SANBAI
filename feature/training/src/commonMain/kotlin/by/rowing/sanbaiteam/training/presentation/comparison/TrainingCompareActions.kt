package by.rowing.sanbaiteam.training.presentation.comparison

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware

internal interface TrainingCompareActions : NavigateBackAware {
    fun onToggleSelection(selectedTrainingId: Long)
    fun onConfirm()
    fun onReset()
}