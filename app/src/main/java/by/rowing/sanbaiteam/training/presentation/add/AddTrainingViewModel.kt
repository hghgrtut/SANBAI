package by.rowing.sanbaiteam.training.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachCsvImport
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachCsvParser
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachImportStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

internal class AddTrainingViewModel(
    private val athleteRepository: AthletesRepository,
    private val trainingRepository: TrainingRepository,
    private val speedCoachImportStorage: SpeedCoachImportStorage,
    private val composeNavigator: ComposeNavigator,
    importUri: String?,
) : ComposeBaseViewModel<AddTrainingState>(
    initialState = run {
        val dateMillis = System.currentTimeMillis()
        AddTrainingState(
            dateMillis = dateMillis,
            dateFormatted = TimeUtils.formatMillisToString(dateMillis)
        )
    }
) {
    private var pendingInitialImportUri: String? = importUri

    init {
        viewModelScope.launch {
            athleteRepository.allAthletes.collectLatest { athletes ->
                changeState { copy(athletesCatalog = athletes) }
            }
        }
        if (!importUri.isNullOrBlank()) {
            changeState {
                copy(importNotice = "Готов к импорту из выбранного файла.")
            }
        }
    }

    fun consumeInitialImportUri(): String? = pendingInitialImportUri.also { pendingInitialImportUri = null }

    fun onDatePicked(year: Int, month: Int, dayOfMonth: Int) {
        val millis = toTrainingDateMillis(year, month, dayOfMonth)
        changeState {
            copy(
                dateMillis = millis,
                dateFormatted = TimeUtils.formatMillisToString(millis)
            )
        }
    }

    fun changeAthleteId(crewLocalId: Long, athleteId: Long) {
        changeState {
            copy(
                crewAthletes = crewAthletes.map { athlete ->
                    if (athlete.localId == crewLocalId) athlete.copy(athleteId = athleteId) else athlete
                },
                validationError = null
            )
        }
    }

    fun addCrewAthlete() {
        changeState {
            if (crewAthletes.size >= AddTrainingState.MAX_CREW_SIZE) {
                copy(validationError = "В экипаже максимум ${AddTrainingState.MAX_CREW_SIZE} спортсменов")
            } else {
                copy(
                    crewAthletes = crewAthletes + AddCrewAthlete(),
                    validationError = null
                )
            }
        }
    }

    fun removeCrewAthlete(crewLocalId: Long) {
        changeState {
            if (crewAthletes.size <= 1) this
            else copy(
                crewAthletes = crewAthletes.filterNot { it.localId == crewLocalId },
                validationError = null
            )
        }
    }

    fun changePieceDistance(pieceLocalId: Long, text: String) {
        updatePiece(pieceLocalId) { piece ->
            val distance = text.filter { it.isDigit() }.toIntOrNull() ?: 0
            piece.copy(distanceText = text.filter { it.isDigit() }, distanceMeters = distance)
        }
    }

    fun changePieceTime(pieceLocalId: Long, text: String) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(
                timeText = text,
                timeMillis = RowingTimeFormat.parseDuration(text) ?: 0L
            )
        }
    }

    fun changePieceStrokeRate(pieceLocalId: Long, text: String) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(
                strokeRateText = text,
                strokeRate = RowingTimeFormat.parseStrokeRate(text) ?: 0.0
            )
        }
    }

    fun addPiece() {
        changeState {
            val template = pieces.lastOrNull()
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
            copy(pieces = pieces + newPiece, validationError = null)
        }
    }

    fun removePiece(pieceLocalId: Long) {
        changeState {
            if (pieces.size <= 1) this
            else copy(
                pieces = pieces.filterNot { it.localId == pieceLocalId },
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
            val piecesToSave = state.crewAthletes.flatMap { crewAthlete ->
                state.pieces.mapIndexed { index, piece ->
                    TrainingAthletePieceEntity(
                        trainingId = 0,
                        athleteId = crewAthlete.athleteId,
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
                val trainingId = trainingRepository.saveTraining(
                    dateMillis = state.dateMillis,
                    type = TrainingPieceType.SINGLE,
                    pieces = piecesToSave,
                    sourceDeviceSerial = state.pendingSourceSerial,
                    sourceSessionName = state.pendingSourceSessionName,
                )
                if (state.pendingRawCsv != null) {
                    val relativePath = speedCoachImportStorage.saveCsv(
                        trainingId = trainingId,
                        serial = state.pendingSourceSerial,
                        csvText = state.pendingRawCsv
                    )
                    trainingRepository.updateTrainingImportMetadata(
                        trainingId = trainingId,
                        relativePath = relativePath,
                        deviceSerial = state.pendingSourceSerial,
                        sessionName = state.pendingSourceSessionName
                    )
                }
            }
            composeNavigator.navigateBack()
        }
    }

    fun onBackClick() {
        composeNavigator.navigateBack()
    }

    fun clearValidationError() {
        changeState { copy(validationError = null, importNotice = null) }
    }

    fun importCsv(csvText: String) {
        viewModelScope.launch {
            applyParsedImport(
                parsed = runCatching { SpeedCoachCsvParser.parse(csvText) }.getOrElse {
                    changeState { copy(importNotice = "Не удалось разобрать CSV файл.") }
                    return@launch
                }
            )
        }
    }

    private suspend fun applyParsedImport(parsed: SpeedCoachCsvImport) {
        val parsedAthleteId = parsed.deviceSerial
            ?.let { athleteRepository.findAthleteIdBySpeedCoachSerial(it) }
            ?: 0L
        val pieces = parsed.intervals.map { interval ->
            AddPieceDraft(
                distanceMeters = interval.distanceMeters,
                timeMillis = interval.timeMillis,
                strokeRate = interval.strokeRate,
                distanceText = interval.distanceMeters.toString(),
                timeText = RowingTimeFormat.formatDuration(interval.timeMillis),
                strokeRateText = RowingTimeFormat.formatStrokeRate(interval.strokeRate),
            )
        }
        val trainingDateMillis = parsed.startTimeMillis?.let(::toTrainingDateMillis) ?: state.dateMillis
        changeState {
            copy(
                dateMillis = trainingDateMillis,
                dateFormatted = TimeUtils.formatMillisToString(trainingDateMillis),
                crewAthletes = listOf(AddCrewAthlete(athleteId = parsedAthleteId)),
                pieces = pieces.ifEmpty { listOf(AddPieceDraft()) },
                pendingRawCsv = parsed.rawCsv,
                pendingSourceSerial = parsed.deviceSerial,
                pendingSourceSessionName = parsed.sessionName,
                importNotice = if (parsedAthleteId == 0L && !parsed.deviceSerial.isNullOrBlank()) {
                    "Спортсмен со спидкоучем ${parsed.deviceSerial} не найден. Выберите вручную."
                } else {
                    "CSV импортирован."
                },
                validationError = null
            )
        }
    }

    private fun toTrainingDateMillis(startTimeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startTimeMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun toTrainingDateMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun validate(): String? {
        if (state.crewAthletes.isEmpty()) return "Добавьте хотя бы одного спортсмена"
        if (state.crewAthletes.size > AddTrainingState.MAX_CREW_SIZE) {
            return "В экипаже максимум ${AddTrainingState.MAX_CREW_SIZE} спортсменов"
        }
        val usedAthletes = mutableSetOf<Long>()
        for (athlete in state.crewAthletes) {
            if (athlete.athleteId <= 0) return "Выберите спортсмена"
            if (!usedAthletes.add(athlete.athleteId)) return "Спортсмен уже добавлен в экипаж"
        }
        if (state.pieces.isEmpty()) return "Добавьте хотя бы один кусок"
        for (piece in state.pieces) {
            val time = RowingTimeFormat.parseDuration(piece.timeText) ?: piece.timeMillis
            val rate = RowingTimeFormat.parseStrokeRate(piece.strokeRateText) ?: piece.strokeRate
            if (piece.distanceMeters <= 0) return "Укажите дистанцию куска"
            if (time <= 0) return "Укажите время куска (м:сс.д)"
            if (rate <= 0) return "Укажите частоту гребков"
        }
        return null
    }

    private fun updatePiece(
        pieceLocalId: Long,
        transform: (AddPieceDraft) -> AddPieceDraft
    ) {
        changeState {
            copy(
                pieces = pieces.map { piece ->
                    if (piece.localId == pieceLocalId) transform(piece) else piece
                },
                validationError = null
            )
        }
    }
}
