package by.rowing.sanbaiteam.training.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

internal class AddTrainingViewModel(
    private val athleteRepository: AthletesRepository,
    private val trainingRepository: TrainingRepository,
    private val composeNavigator: ComposeNavigator,
    trainingDateMillis: Long
) : ComposeBaseViewModel<AddTrainingState>(
    initialState = AddTrainingState(
        dateMillis = trainingDateMillis,
        dateFormatted = TimeUtils.formatMillisToString(trainingDateMillis)
    )
) {

    init {
        viewModelScope.launch {
            athleteRepository.allAthletes.collectLatest { athletes ->
                changeState { copy(athletesCatalog = athletes) }
            }
        }
    }

    fun onDatePicked(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val millis = calendar.timeInMillis
        changeState {
            copy(
                dateMillis = millis,
                dateFormatted = TimeUtils.formatMillisToString(millis)
            )
        }
    }

    fun changeAthleteId(sessionLocalId: Long, athleteId: Long) {
        updateSession(sessionLocalId) { it.copy(athleteId = athleteId) }
    }

    fun changePieceDistance(sessionLocalId: Long, pieceLocalId: Long, text: String) {
        updatePiece(sessionLocalId, pieceLocalId) { piece ->
            val distance = text.filter { it.isDigit() }.toIntOrNull() ?: 0
            piece.copy(distanceText = text.filter { it.isDigit() }, distanceMeters = distance)
        }
    }

    fun changePieceTime(sessionLocalId: Long, pieceLocalId: Long, text: String) {
        updatePiece(sessionLocalId, pieceLocalId) { piece ->
            piece.copy(
                timeText = text,
                timeMillis = RowingTimeFormat.parseDuration(text) ?: 0L
            )
        }
    }

    fun changePieceStrokeRate(sessionLocalId: Long, pieceLocalId: Long, text: String) {
        updatePiece(sessionLocalId, pieceLocalId) { piece ->
            piece.copy(
                strokeRateText = text,
                strokeRate = RowingTimeFormat.parseStrokeRate(text) ?: 0.0
            )
        }
    }

    fun addPiece(sessionLocalId: Long) {
        updateSession(sessionLocalId) { session ->
            val template = session.pieces.lastOrNull()
            val newPiece = if (template != null) {
                AddPieceDraft(
                    distanceMeters = template.distanceMeters,
                    timeMillis = template.timeMillis,
                    strokeRate = template.strokeRate,
                    distanceText = template.distanceText,
                    timeText = template.timeText,
                    strokeRateText = template.strokeRateText
                )
            } else {
                AddPieceDraft()
            }
            session.copy(pieces = session.pieces + newPiece)
        }
    }

    fun removePiece(sessionLocalId: Long, pieceLocalId: Long) {
        updateSession(sessionLocalId) { session ->
            if (session.pieces.size <= 1) session
            else session.copy(pieces = session.pieces.filterNot { it.localId == pieceLocalId })
        }
    }

    fun addAthleteSession() {
        changeState {
            val template = athleteSessions.lastOrNull()?.pieces.orEmpty()
            val copiedPieces = if (template.isEmpty()) {
                listOf(AddPieceDraft())
            } else {
                template.map { piece ->
                    AddPieceDraft(
                        distanceMeters = piece.distanceMeters,
                        timeMillis = piece.timeMillis,
                        strokeRate = piece.strokeRate,
                        distanceText = piece.distanceText,
                        timeText = piece.timeText,
                        strokeRateText = piece.strokeRateText
                    )
                }
            }
            copy(
                athleteSessions = athleteSessions + AddAthleteSession(pieces = copiedPieces),
                validationError = null
            )
        }
    }

    fun removeAthleteSession(sessionLocalId: Long) {
        changeState {
            if (athleteSessions.size <= 1) this
            else copy(
                athleteSessions = athleteSessions.filterNot { it.localId == sessionLocalId },
                validationError = null
            )
        }
    }

    fun saveTraining() {
        val error = validate()
        if (error != null) {
            changeState { copy(validationError = error) }
            return
        }
        viewModelScope.launch {
            val pieces = state.athleteSessions.flatMap { session ->
                session.pieces.mapIndexed { index, piece ->
                    TrainingAthletePieceEntity(
                        trainingId = 0,
                        athleteId = session.athleteId,
                        order = index,
                        distanceMeters = piece.distanceMeters,
                        timeMillis = RowingTimeFormat.parseDuration(piece.timeText)
                            ?: piece.timeMillis,
                        strokeRate = RowingTimeFormat.parseStrokeRate(piece.strokeRateText)
                            ?: piece.strokeRate
                    )
                }
            }
            withContext(Dispatchers.IO) {
                trainingRepository.saveTraining(
                    dateMillis = state.dateMillis,
                    type = TrainingPieceType.SINGLE,
                    pieces = pieces
                )
            }
            composeNavigator.navigateBack()
        }
    }

    fun onBackClick() {
        composeNavigator.navigateBack()
    }

    fun clearValidationError() {
        changeState { copy(validationError = null) }
    }

    private fun validate(): String? {
        if (state.athleteSessions.isEmpty()) return "Добавьте хотя бы одного спортсмена"
        val usedAthletes = mutableSetOf<Long>()
        for (session in state.athleteSessions) {
            if (session.athleteId <= 0) return "Выберите спортсмена"
            if (!usedAthletes.add(session.athleteId)) return "Спортсмен уже добавлен в тренировку"
            if (session.pieces.isEmpty()) return "У спортсмена должен быть хотя бы один кусок"
            for (piece in session.pieces) {
                val time = RowingTimeFormat.parseDuration(piece.timeText) ?: piece.timeMillis
                val rate = RowingTimeFormat.parseStrokeRate(piece.strokeRateText) ?: piece.strokeRate
                if (piece.distanceMeters <= 0) return "Укажите дистанцию куска"
                if (time <= 0) return "Укажите время куска (м:сс.д)"
                if (rate <= 0) return "Укажите частоту гребков"
            }
        }
        return null
    }

    private fun updateSession(
        sessionLocalId: Long,
        transform: (AddAthleteSession) -> AddAthleteSession
    ) {
        changeState {
            copy(
                athleteSessions = athleteSessions.map { session ->
                    if (session.localId == sessionLocalId) transform(session) else session
                },
                validationError = null
            )
        }
    }

    private fun updatePiece(
        sessionLocalId: Long,
        pieceLocalId: Long,
        transform: (AddPieceDraft) -> AddPieceDraft
    ) {
        updateSession(sessionLocalId) { session ->
            session.copy(
                pieces = session.pieces.map { piece ->
                    if (piece.localId == pieceLocalId) transform(piece) else piece
                }
            )
        }
    }
}
