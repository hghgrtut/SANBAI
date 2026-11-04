package by.rowing.sanbaiteam.training.data.models.chart

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

private const val ROW_ERG_PACE_DISTANCE_METERS = 500
private const val BIKE_ERG_PACE_DISTANCE_METERS = 1000

/**
 * Дистанция, на которую считается темп в графике.
 *
 * BikeErg показывает темп на 1000 м: скорости вело и гребли различаются примерно вдвое,
 * поэтому на общей шкале «на 500 м» вело-значения выглядят неправдоподобно маленькими.
 */
internal fun chartPaceDistanceMeters(pieceType: TrainingPieceType?): Int =
    if (pieceType == TrainingPieceType.CONCEPT_BIKE_ERG) BIKE_ERG_PACE_DISTANCE_METERS else ROW_ERG_PACE_DISTANCE_METERS