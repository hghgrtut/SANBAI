package by.rowing.sanbaiteam.training.data.models.detail

/** Показатель отрезка, который можно скрыть на экране детали тренировки. */
internal enum class TrainingDetailField {
    DISTANCE,
    TIME,
    PACE,
    STROKE_RATE,
    AVG_HEART_RATE,
    MAX_HEART_RATE,
    RECOVERY_HEART_RATE,
    LACTATE,
    PERCENT_OF_PB,
}

/**
 * Разрешено ли переключить [field]: последний видимый показатель скрыть нельзя.
 */
internal fun Set<TrainingDetailField>.canToggle(field: TrainingDetailField): Boolean =
    field in this || size < TrainingDetailField.entries.size - 1
