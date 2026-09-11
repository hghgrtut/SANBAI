package by.rowing.sanbaiteam.training.data.entity

import by.rowing.sanbaiteam.data.local.R

enum class TrainingPieceType(
    val uiResId: Int,
    val seatsCount: Int = 1
) {

    REST(
        uiResId = R.string.training_piece_type_rest,
    ),
    ERGO(
        uiResId = R.string.training_piece_type_ergo,
    ),
    BIKE(
        uiResId = R.string.training_piece_type_bike,
    ),
    SINGLE(
        uiResId = R.string.training_piece_type_single,
    ),
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
    );
}