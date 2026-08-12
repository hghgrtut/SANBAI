package by.rowing.sanbaiteam.training.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.AndroidResourceUtils
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachCsvImport
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachCsvMerger
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachCsvParser
import by.rowing.sanbaiteam.training.data.speedcoach.SpeedCoachImportStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

internal class AddTrainingViewModel(
    private val resourceUtils: AndroidResourceUtils,
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
            changeState { copy(importNotice = resourceUtils.getString(R.string.add_training_import_ready)) }
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
                copy(
                    validationError = resourceUtils.getString(
                        R.string.add_training_crew_max_size,
                        AddTrainingState.MAX_CREW_SIZE
                    )
                )
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
                if (state.pendingRawCsvs.isNotEmpty()) {
                    val mergedCsv = SpeedCoachCsvMerger.merge(state.pendingRawCsvs)
                    val relativePath = speedCoachImportStorage.saveCsv(
                        trainingId = trainingId,
                        serial = state.pendingSourceSerial,
                        csvText = mergedCsv
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
        importCsvBatch(listOf(csvText))
    }

    fun importCsvBatch(csvTexts: List<String>) {
        if (csvTexts.isEmpty()) return
        viewModelScope.launch {
            for (csvText in csvTexts) {
                val parsed = runCatching { SpeedCoachCsvParser.parse(csvText) }.getOrElse {
                    changeState {
                        copy(importNotice = resourceUtils.getString(R.string.add_training_import_parse_error))
                    }
                    return@launch
                }
                applyParsedImport(parsed)
            }
        }
    }

    private suspend fun applyParsedImport(parsed: SpeedCoachCsvImport) {
        val newPieces = parsed.intervals.map { interval ->
            AddPieceDraft(
                distanceMeters = interval.distanceMeters,
                timeMillis = interval.timeMillis,
                strokeRate = interval.strokeRate,
                distanceText = interval.distanceMeters.toString(),
                timeText = RowingTimeFormat.formatDuration(interval.timeMillis),
                strokeRateText = RowingTimeFormat.formatStrokeRate(interval.strokeRate),
            )
        }
        val isFirstImport = state.pendingRawCsvs.isEmpty()

        if (isFirstImport) {
            val parsedAthleteId = parsed.deviceSerial
                ?.let { athleteRepository.findAthleteIdBySpeedCoachSerial(it) }
                ?: 0L
            val trainingDateMillis = parsed.startTimeMillis?.let(::toTrainingDateMillis) ?: state.dateMillis
            changeState {
                copy(
                    dateMillis = trainingDateMillis,
                    dateFormatted = TimeUtils.formatMillisToString(trainingDateMillis),
                    crewAthletes = listOf(AddCrewAthlete(athleteId = parsedAthleteId)),
                    pieces = newPieces.ifEmpty { listOf(AddPieceDraft()) },
                    pendingRawCsvs = listOf(parsed.rawCsv),
                    pendingSourceSerial = parsed.deviceSerial,
                    pendingSourceSessionName = parsed.sessionName,
                    importNotice = buildImportNotice(
                        fileCount = 1,
                        pieceCount = newPieces.size,
                        serialNotFound = parsedAthleteId == 0L && !parsed.deviceSerial.isNullOrBlank(),
                        deviceSerial = parsed.deviceSerial,
                    ),
                    validationError = null
                )
            }
        } else {
            val fileCount = state.pendingRawCsvs.size + 1
            val pieceCount = state.pieces.size + newPieces.size
            changeState {
                copy(
                    pieces = pieces + newPieces,
                    pendingRawCsvs = pendingRawCsvs + parsed.rawCsv,
                    importNotice = buildImportNotice(
                        fileCount = fileCount,
                        pieceCount = pieceCount,
                        serialNotFound = false,
                        deviceSerial = null,
                    ),
                    validationError = null
                )
            }
        }
    }

    private fun buildImportNotice(
        fileCount: Int,
        pieceCount: Int,
        serialNotFound: Boolean,
        deviceSerial: String?,
    ): String {
        if (serialNotFound && deviceSerial != null) {
            return resourceUtils.getString(
                R.string.add_training_import_not_found_by_serial,
                deviceSerial
            )
        }
        return if (fileCount > 1) {
            resourceUtils.getString(
                R.string.add_training_import_multi_success,
                fileCount,
                pieceCount
            )
        } else {
            resourceUtils.getString(R.string.add_training_import_success)
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
        if (state.crewAthletes.isEmpty()) {
            return resourceUtils.getString(R.string.add_training_validation_need_athlete)
        }
        if (state.crewAthletes.size > AddTrainingState.MAX_CREW_SIZE) {
            return resourceUtils.getString(
                R.string.add_training_crew_max_size,
                AddTrainingState.MAX_CREW_SIZE
            )
        }
        val usedAthletes = mutableSetOf<Long>()
        for (athlete in state.crewAthletes) {
            if (athlete.athleteId <= 0) {
                return resourceUtils.getString(R.string.add_training_validation_select_athlete)
            }
            if (!usedAthletes.add(athlete.athleteId)) {
                return resourceUtils.getString(R.string.add_training_validation_athlete_duplicate)
            }
        }
        if (state.pieces.isEmpty()) {
            return resourceUtils.getString(R.string.add_training_validation_need_piece)
        }
        for (piece in state.pieces) {
            val time = RowingTimeFormat.parseDuration(piece.timeText) ?: piece.timeMillis
            val rate = RowingTimeFormat.parseStrokeRate(piece.strokeRateText) ?: piece.strokeRate
            when {
                piece.distanceMeters <= 0 ->
                    return resourceUtils.getString(R.string.add_training_validation_piece_distance)
                time <= 0 -> return resourceUtils.getString(R.string.add_training_validation_piece_time)
                rate <= 0 -> return resourceUtils.getString(R.string.add_training_validation_piece_stroke_rate)
            }
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
