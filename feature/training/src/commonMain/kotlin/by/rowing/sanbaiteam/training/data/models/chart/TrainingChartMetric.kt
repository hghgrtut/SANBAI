package by.rowing.sanbaiteam.training.data.models.chart

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_detail_chart_heart_rate
import by.rowing.sanbaiteam.training.resources.training_detail_chart_speed
import by.rowing.sanbaiteam.training.resources.training_detail_chart_stroke_rate
import org.jetbrains.compose.resources.StringResource

internal enum class TrainingChartMetric(
    val icon: ImageVector,
    val description: StringResource
) {
    SPEED(
        icon = Icons.Filled.Speed,
        description = Res.string.training_detail_chart_speed
    ),
    STROKE_RATE(
        icon = Icons.Filled.Timeline,
        description = Res.string.training_detail_chart_stroke_rate
    ),
    HEART_RATE(
        icon = Icons.Filled.MonitorHeart,
        description = Res.string.training_detail_chart_heart_rate
    ),
}