package by.rowing.sanbaiteam.training.data.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Мощность в ваттах.
 *
 * Concept2 считает ватты из скорости по формуле `W = 2.8 / (pace / 500)³`. Для RowErg темп берётся
 * на 500 м, для BikeErg — на 1000 м, поэтому при одинаковой скорости BikeErg показывает в 8 раз
 * меньше ватт (`0.35·v³` вместо `2.8·v³`).
 *
 * Расчёт используется только там, где мощность не пришла из файла (ручной ввод, потерянный
 * исходник). RP3 пишет измеренную мощность, и формула Concept2 для него не подходит.
 */
internal object TrainingPower {

    private const val MILLIS_PER_SECOND = 1000.0
    private const val ROW_ERG_COEFFICIENT = 2.80
    private const val BIKE_ERG_COEFFICIENT = 0.35

    fun estimatedWatts(
        pieceType: TrainingPieceType?,
        distanceMeters: Int,
        timeMillis: Long,
    ): Int? {
        if (distanceMeters <= 0 || timeMillis <= 0L) return null
        val coefficient = coefficientOf(pieceType) ?: return null
        val speed = distanceMeters * MILLIS_PER_SECOND / timeMillis
        return (coefficient * speed.pow(3)).roundToInt().takeIf { it > 0 }
    }

    private fun coefficientOf(pieceType: TrainingPieceType?): Double? = when (pieceType) {
        TrainingPieceType.CONCEPT_ROW_ERG -> ROW_ERG_COEFFICIENT
        TrainingPieceType.CONCEPT_BIKE_ERG -> BIKE_ERG_COEFFICIENT
        else -> null
    }
}