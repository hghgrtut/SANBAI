package by.rowing.sanbaiteam.training.presentation.list.models

import androidx.annotation.StringRes

internal data class ListTrainingStateItem(
    val trainingId: Long,
    @StringRes val trainingTypeResId: Int,
    val trainingDate: String,
    val trainingWork: String,
    val athletes: List<String>,
)