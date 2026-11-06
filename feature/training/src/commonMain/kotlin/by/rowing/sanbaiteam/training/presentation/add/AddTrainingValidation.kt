package by.rowing.sanbaiteam.training.presentation.add

import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.add_training_crew_max_size
import by.rowing.sanbaiteam.training.resources.add_training_validation_athlete_duplicate
import by.rowing.sanbaiteam.training.resources.add_training_validation_need_athlete
import by.rowing.sanbaiteam.training.resources.add_training_validation_need_piece
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_distance
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_stroke_rate
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_time
import by.rowing.sanbaiteam.training.resources.add_training_validation_select_athlete
import by.rowing.sanbaiteam.uikit.component.UiText

/** Ошибки полей одного отрезка: поле заполнено неверно, если ошибка не null. */
internal data class AddPieceFieldErrors(
    val distance: UiText? = null,
    val time: UiText? = null,
    val strokeRate: UiText? = null,
) {

    val firstError: UiText? get() = distance ?: time ?: strokeRate
    val hasErrors: Boolean get() = firstError != null
}

/**
 * Результат проверки формы перед сохранением.
 *
 * [generalError] — ошибка, которую нельзя показать у конкретного поля (например, экипаж целиком).
 * [athleteError] — ошибка выбора спортсмена, показывается под дропдауном.
 * [pieceErrors] — ошибки по отрезкам, ключ — [AddPieceDraft.localId]; порядок совпадает с порядком отрезков.
 */
internal data class AddTrainingValidation(
    val generalError: UiText? = null,
    val athleteError: UiText? = null,
    val pieceErrors: Map<Long, AddPieceFieldErrors> = emptyMap(),
) {

    val isValid: Boolean get() = generalError == null && athleteError == null && pieceErrors.isEmpty()
    val firstInvalidPieceId: Long? get() = pieceErrors.keys.firstOrNull()
}

internal fun validateAddTraining(
    crewAthletes: List<AddCrewAthlete>,
    pieces: List<AddPieceDraft>,
): AddTrainingValidation {
    val crewError = validateCrew(crewAthletes)
    return when {
        crewError != null -> AddTrainingValidation(generalError = crewError)
        pieces.isEmpty() ->
            AddTrainingValidation(generalError = UiText.of(Res.string.add_training_validation_need_piece))

        else -> AddTrainingValidation(
            athleteError = validateAthleteSelection(crewAthletes),
            pieceErrors = validatePieces(pieces),
        )
    }
}

private fun validateCrew(crewAthletes: List<AddCrewAthlete>): UiText? = when {
    crewAthletes.isEmpty() -> UiText.of(Res.string.add_training_validation_need_athlete)
    crewAthletes.size > AddTrainingState.MAX_CREW_SIZE -> UiText.of(
        Res.string.add_training_crew_max_size,
        AddTrainingState.MAX_CREW_SIZE,
    )

    else -> null
}

private fun validateAthleteSelection(crewAthletes: List<AddCrewAthlete>): UiText? {
    val usedAthleteIds = mutableSetOf<Long>()
    for (athlete in crewAthletes) {
        if (athlete.rowerId <= 0) {
            return UiText.of(Res.string.add_training_validation_select_athlete)
        }
        if (!usedAthleteIds.add(athlete.rowerId)) {
            return UiText.of(Res.string.add_training_validation_athlete_duplicate)
        }
    }
    return null
}

private fun validatePieces(pieces: List<AddPieceDraft>): Map<Long, AddPieceFieldErrors> = pieces.mapNotNull { piece ->
    val errors = piece.validate()
    if (errors.hasErrors) piece.localId to errors else null
}.toMap()

private fun AddPieceDraft.validate(): AddPieceFieldErrors = AddPieceFieldErrors(
    distance = UiText.of(Res.string.add_training_validation_piece_distance)
        .takeIf { distanceMeters <= 0 },
    time = UiText.of(Res.string.add_training_validation_piece_time)
        .takeIf { (RowingTimeFormat.parseTimeInput(input = timeText) ?: timeMillis) <= 0 },
    strokeRate = UiText.of(Res.string.add_training_validation_piece_stroke_rate)
        .takeIf { (RowingTimeFormat.parseStrokeRate(input = strokeRateText) ?: strokeRate) <= 0 },
)