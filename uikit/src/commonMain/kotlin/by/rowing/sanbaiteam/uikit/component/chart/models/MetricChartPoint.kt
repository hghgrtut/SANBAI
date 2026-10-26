package by.rowing.sanbaiteam.uikit.component.chart.models

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
data class MetricChartPoint(
    val elapsedMillis: Long,
    val value: Double,
)