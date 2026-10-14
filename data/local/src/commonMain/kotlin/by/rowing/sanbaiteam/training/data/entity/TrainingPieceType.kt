package by.rowing.sanbaiteam.training.data.entity

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.data.local.resources.Res
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_concept_bike_erg
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_concept_row_erg
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_double
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_eight
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_four
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_pair
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_quadruple
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_rp3
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_single
import by.rowing.sanbaiteam.data.local.resources.training_piece_type_watt_bike
import org.jetbrains.compose.resources.StringResource

@AllowDetektPublic
enum class TrainingPieceType(
    val uiResId: StringResource,
    val seatsCount: Int = 1
) {
    CONCEPT_ROW_ERG(uiResId = Res.string.training_piece_type_concept_row_erg),
    RP_3(uiResId = Res.string.training_piece_type_rp3),
    CONCEPT_BIKE_ERG(uiResId = Res.string.training_piece_type_concept_bike_erg),
    WATT_BIKE(uiResId = Res.string.training_piece_type_watt_bike),
    SINGLE(uiResId = Res.string.training_piece_type_single),
    DOUBLE(
        uiResId = Res.string.training_piece_type_double,
        seatsCount = 2,
    ),
    QUADRUPLE(
        uiResId = Res.string.training_piece_type_quadruple,
        seatsCount = 4
    ),
    PAIR(
        uiResId = Res.string.training_piece_type_pair,
        seatsCount = 2
    ),
    FOUR(
        uiResId = Res.string.training_piece_type_four,
        seatsCount = 4
    ),
    EIGHT(
        uiResId = Res.string.training_piece_type_eight,
        seatsCount = 8
    )
}
