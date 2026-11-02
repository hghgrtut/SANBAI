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
    val isWater: Boolean,
) {
    CONCEPT_ROW_ERG(
        uiResId = Res.string.training_piece_type_concept_row_erg,
        isWater = false
    ),
    RP_3(
        uiResId = Res.string.training_piece_type_rp3,
        isWater = false
    ),
    CONCEPT_BIKE_ERG(
        uiResId = Res.string.training_piece_type_concept_bike_erg,
        isWater = false
    ),
    WATT_BIKE(
        uiResId = Res.string.training_piece_type_watt_bike,
        isWater = false
    ),
    SINGLE(
        uiResId = Res.string.training_piece_type_single,
        isWater = true,
    ),
    DOUBLE(
        uiResId = Res.string.training_piece_type_double,
        isWater = true,
    ),
    QUADRUPLE(
        uiResId = Res.string.training_piece_type_quadruple,
        isWater = true,
    ),
    PAIR(
        uiResId = Res.string.training_piece_type_pair,
        isWater = true,
    ),
    FOUR(
        uiResId = Res.string.training_piece_type_four,
        isWater = true,
    ),
    EIGHT(
        uiResId = Res.string.training_piece_type_eight,
        isWater = true,
    )
}