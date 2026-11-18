package by.rowing.sanbaiteam.athlete.presentation.chart

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightMath
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightRecord
import by.rowing.sanbaiteam.athlete.domain.weight.TENTHS_IN_KILOGRAM
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.uikit.component.chart.models.MetricChartPoint
import kotlinx.coroutines.launch

internal class AthleteWeightChartViewModel(
    private val athletesRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator,
    private val athleteId: Long,
    private val fromMillis: Long,
    private val toMillis: Long,
) : ComposeBaseViewModel<AthleteWeightChartState>(
    initialState = AthleteWeightChartState(
        fromMillis = fromMillis,
        toMillis = toMillis,
    )
), NavigateBackAware {

    init {
        viewModelScope.launch { load() }
    }

    override fun navigateBack() {
        composeNavigator.navigateBack()
    }

    private suspend fun load() {
        if (athletesRepository.getAthlete(athleteId) == null) {
            changeState {
                copy(
                    isLoading = false,
                    notFound = true,
                )
            }
            return
        }
        val records = athletesRepository.getWeights(athleteId).map { entity ->
            AthleteWeightRecord(
                dateMillis = entity.dateMillis,
                weightTenths = entity.weightTenths,
            )
        }
        val from = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        val timeTicks = AthleteWeightMath.dateTickOffsets(
            fromMillis = fromMillis,
            toMillis = toMillis,
        )
        val points = AthleteWeightMath.chartPoints(
            records = records,
            fromMillis = from,
            toMillis = toMillis,
        ).map { point ->
            MetricChartPoint(
                elapsedMillis = TimeUtils.startOfDayMillis(timeInMillis = point.dateMillis) - from,
                value = point.weightTenths / TENTHS_IN_KILOGRAM,
            )
        }
        changeState {
            copy(
                durationMillis = timeTicks.lastOrNull() ?: 0L,
                points = points,
                timeTicks = timeTicks,
                averageWeightTenths = AthleteWeightMath.averageWeightTenths(
                    records = records,
                    fromMillis = from,
                    toMillis = toMillis,
                ),
                isLoading = false,
            )
        }
    }
}