package by.rowing.sanbaiteam.training.presentation.detail.compose

internal interface TrainingDetailActions {

    fun onBackClick()

    fun onCompareClick()

    fun onToggleComparison(order: Int)

    fun onClearComparison()
}