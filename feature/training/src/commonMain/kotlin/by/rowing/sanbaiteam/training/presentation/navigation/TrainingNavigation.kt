package by.rowing.sanbaiteam.training.presentation.navigation

import by.rowing.sanbaiteam.core.presentation.compose.NavigationDestination
import kotlinx.serialization.Serializable

internal sealed interface TrainingNavigation {

    @Serializable
    data object TrainingList : NavigationDestination<Unit>

    @Serializable
    data class TrainingAdd(
        val importUri: String? = null,
    ) : NavigationDestination<Unit>

    @Serializable
    data class TrainingDetail(val trainingId: Long) : NavigationDestination<Unit>

    @Serializable
    data class TrainingCompare(val trainingId: Long) : NavigationDestination<Unit>

    @Serializable
    data class TrainingChart(
        val trainingId: Long,
        val order: Int,
        val metric: String,
    ) : NavigationDestination<Unit>
}
