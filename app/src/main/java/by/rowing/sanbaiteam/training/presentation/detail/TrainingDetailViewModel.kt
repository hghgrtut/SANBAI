package by.rowing.sanbaiteam.training.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import kotlinx.coroutines.launch

internal class TrainingDetailViewModel(
    private val repository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    trainingId: Long
) : ComposeBaseViewModel<TrainingDetailState>(
    initialState = TrainingDetailState(trainingId = trainingId)
) {

    init {
        viewModelScope.launch {
            val initialState = repository.getTrainingDetail(trainingId)
            changeState { initialState }
        }
    }

    fun onBackClick() {
        composeNavigator.navigateBack()
    }
}
