package by.rowing.sanbaiteam.training.data.models.detail

import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.training_detail_avg_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_distance_label
import by.rowing.sanbaiteam.training.resources.training_detail_lactate_label
import by.rowing.sanbaiteam.training.resources.training_detail_max_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_pace_label
import by.rowing.sanbaiteam.training.resources.training_detail_percent_of_pb_label
import by.rowing.sanbaiteam.training.resources.training_detail_recovery_heart_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_stroke_rate_label
import by.rowing.sanbaiteam.training.resources.training_detail_time_label
import org.jetbrains.compose.resources.StringResource

/** Показатель отрезка, который можно скрыть на экране детали тренировки. */
internal enum class TrainingDetailField(val labelRes: StringResource) {
    DISTANCE(labelRes = Res.string.training_detail_distance_label),
    TIME(labelRes = Res.string.training_detail_time_label),
    PACE(labelRes = Res.string.training_detail_pace_label),
    STROKE_RATE(labelRes = Res.string.training_detail_stroke_rate_label),
    AVG_HEART_RATE(labelRes = Res.string.training_detail_avg_heart_rate_label),
    MAX_HEART_RATE(labelRes = Res.string.training_detail_max_heart_rate_label),
    RECOVERY_HEART_RATE(labelRes = Res.string.training_detail_recovery_heart_rate_label),
    LACTATE(labelRes = Res.string.training_detail_lactate_label),
    PERCENT_OF_PB(labelRes = Res.string.training_detail_percent_of_pb_label),
}

/** Разрешено ли переключить [field]: последний видимый показатель скрыть нельзя. */
internal fun Set<TrainingDetailField>.canToggle(field: TrainingDetailField): Boolean =
    field in this || size < TrainingDetailField.entries.size - 1