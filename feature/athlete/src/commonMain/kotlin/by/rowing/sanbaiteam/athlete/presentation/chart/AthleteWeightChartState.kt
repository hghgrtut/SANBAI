package by.rowing.sanbaiteam.athlete.presentation.chart

import by.rowing.sanbaiteam.uikit.component.chart.models.MetricChartPoint

internal data class AthleteWeightChartState(
    val fromMillis: Long = 0,
    val toMillis: Long = 0,
    val durationMillis: Long = 0,
    val points: List<MetricChartPoint> = emptyList(),
    val timeTicks: List<Long> = emptyList(),
    val averageWeightTenths: Int? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
)