package by.rowing.sanbaiteam.training.data.entity

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.data.local.R

@AllowDetektPublic
enum class TrainingPieceType(
    val uiResId: Int,
    val seatsCount: Int = 1
) {
    CONCEPT_ROW_ERG(uiResId = R.string.training_piece_type_concept_row_erg),
    RP_3(uiResId = R.string.training_piece_type_rp3),
    CONCEPT_BIKE_ERG(uiResId = R.string.training_piece_type_concept_bike_erg),
    WATT_BIKE(uiResId = R.string.training_piece_type_watt_bike),
    SINGLE(uiResId = R.string.training_piece_type_single),
    DOUBLE(
        uiResId = R.string.training_piece_type_double,
        seatsCount = 2,
    ),
    QUADRUPLE(
        uiResId = R.string.training_piece_type_quadruple,
        seatsCount = 4
    ),
    PAIR(
        uiResId = R.string.training_piece_type_pair,
        seatsCount = 2
    ),
    FOUR(
        uiResId = R.string.training_piece_type_four,
        seatsCount = 4
    ),
    EIGHT(
        uiResId = R.string.training_piece_type_eight,
        seatsCount = 8
    )
}