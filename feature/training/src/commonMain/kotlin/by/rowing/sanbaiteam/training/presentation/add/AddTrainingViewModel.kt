package by.rowing.sanbaiteam.training.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingimport.ParsedTrainingImport
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportParsers
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingimport.impliedTrainingType
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.add_training_crew_max_size
import by.rowing.sanbaiteam.training.resources.add_training_import_mixed_sources
import by.rowing.sanbaiteam.training.resources.add_training_import_multi_success
import by.rowing.sanbaiteam.training.resources.add_training_import_not_found_by_serial
import by.rowing.sanbaiteam.training.resources.add_training_import_parse_error
import by.rowing.sanbaiteam.training.resources.add_training_import_read_error
import by.rowing.sanbaiteam.training.resources.add_training_import_ready
import by.rowing.sanbaiteam.training.resources.add_training_import_success
import by.rowing.sanbaiteam.training.resources.add_training_import_unsupported_format
import by.rowing.sanbaiteam.training.resources.add_training_validation_athlete_duplicate
import by.rowing.sanbaiteam.training.resources.add_training_validation_need_athlete
import by.rowing.sanbaiteam.training.resources.add_training_validation_need_piece
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_distance
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_stroke_rate
import by.rowing.sanbaiteam.training.resources.add_training_validation_piece_time
import by.rowing.sanbaiteam.training.resources.add_training_validation_select_athlete
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

internal class AddTrainingViewModel(
    private val athleteRepository: AthletesRepository,
    private val trainingRepository: TrainingRepository,
    private val trainingImportStorage: TrainingImportStorage,
    private val trainingFileReader: TrainingFileReader,
    private val composeNavigator: ComposeNavigator,
    importUri: String?,
) : ComposeBaseViewModel<AddTrainingState>(
    initialState = run {
        val dateMillis = nowInMillis()
        AddTrainingState(
            dateMillis = dateMillis,
            dateFormatted = TimeUtils.formatDate(dateMillis)
        )
    }
) {

    private var pendingInitialImportUri: String? = importUri

    init {
        viewModelScope.launch {
            athleteRepository.allAthletes.collectLatest { athletes ->
                changeState { copy(rowersCatalog = athletes) }
            }
        }
        if (!importUri.isNullOrBlank()) {
            changeState { copy(importNotice = UiText.of(Res.string.add_training_import_ready)) }
        }
    }

    fun consumeInitialImportUri(): String? = pendingInitialImportUri.also { pendingInitialImportUri = null }

    fun onDatePicked(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        val millis = toTrainingDateMillis(year, month, dayOfMonth)
        changeState {
            copy(
                dateMillis = millis,
                dateFormatted = TimeUtils.formatDate(millis)
            )
        }
    }

    fun changeTrainingType(trainingType: TrainingPieceType) {
        changeState {
            copy(
                trainingType = trainingType,
                validationError = null
            )
        }
    }

    fun changeAthleteId(
        crewLocalId: Long,
        athleteId: Long
    ) {
        changeState {
            copy(
                crewAthletes = crewAthletes.map { athlete ->
                    if (athlete.crewId == crewLocalId) athlete.copy(rowerId = athleteId) else athlete
                },
                validationError = null
            )
        }
    }

    fun changePieceAvgHeartRate(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(avgHeartRate = parseIntFromText(text))
        }
    }

    fun changePieceMaxHeartRate(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(maxHeartRate = parseIntFromText(text))
        }
    }

    fun changePieceRecoveryHeartRate(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(recoveryHeartRate = parseIntFromText(text))
        }
    }

    fun changePieceLactate(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            piece.copy(
                lactateText = text,
                lactate = parseLactateFromText(text)
            )
        }
    }

    fun changePieceDistance(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            val distance = parseIntFromText(text) ?: 0
            piece.copy(
                distanceText = text.filter { it.isDigit() },
                distanceMeters = distance
            )
        }
    }

    fun changePieceTime(
        pieceLocalId: Long,
        text: String
    ) {
        updatePiece(pieceLocalId) { piece ->
            val sanitized = RowingTimeFormat.sanitizeTimeInput(text = text)
            piece.copy(
                timeText = sanitized,
                timeMillis = RowingTimeFormat.parseTimeInput(input = sanitized) ?: 0L
            )
        }
    }

    fun changePieceStrokeRate(
        pieceLocalId: Long,
        text: String
    ) {
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
                    avgHeartRate = template.avgHeartRate,
                    maxHeartRate = template.maxHeartRate,
                    recoveryHeartRate = template.recoveryHeartRate,
                    lactate = template.lactate,
                    distanceText = template.distanceText,
                    timeText = template.timeText,
                    strokeRateText = template.strokeRateText,
                    lactateText = template.lactateText,
                )
            } else {
                AddPieceDraft()
            }
            copy(
                pieces = pieces + newPiece,
                validationError = null
            )
        }
    }

    fun removePiece(pieceLocalId: Long) {
        if (state.pieces.size > 1) {
            changeState {
                copy(
                    pieces = pieces.filterNot { it.localId == pieceLocalId },
                    validationError = null
                )
            }
        }
    }

    fun saveTraining() {
        val error = validate()
        if (error != null) {
            changeState { copy(validationError = error) }
            return
        }
        viewModelScope.launch(context = Dispatchers.Default) {
            val piecesToSave = state.crewAthletes.flatMap { crewAthlete ->
                state.pieces.mapIndexed { index, piece ->
                    TrainingAthletePieceEntity(
                        trainingId = 0,
                        athleteId = crewAthlete.rowerId,
                        order = index,
                        distanceMeters = piece.distanceMeters,
                        timeMillis = RowingTimeFormat.parseTimeInput(input = piece.timeText) ?: piece.timeMillis,
                        strokeRate = RowingTimeFormat.parseStrokeRate(input = piece.strokeRateText) ?: piece.strokeRate,
                        avgHeartRate = piece.avgHeartRate,
                        maxHeartRate = piece.maxHeartRate,
                        recoveryHeartRate = piece.recoveryHeartRate,
                        lactate = parseLactateFromText(piece.lactateText) ?: piece.lactate,
                    )
                }
            }
            val trainingId = trainingRepository.saveTraining(
                dateMillis = state.dateMillis,
                type = state.trainingType,
                pieces = piecesToSave,
                sourceDeviceSerial = state.pendingSourceSerial,
                sourceSessionName = state.pendingSourceSessionName,
            )
            if (state.pendingRawCsvs.isNotEmpty()) {
                val importSource = state.pendingImportSource ?: TrainingImportSource.SPEED_COACH
                val mergedCsv = TrainingImportParsers.bySource(importSource).merge(state.pendingRawCsvs)
                val relativePath = trainingImportStorage.saveCsv(
                    trainingId = trainingId,
                    source = importSource,
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
            Dispatchers.Main.immediate(composeNavigator::navigateBack)
        }
    }

    fun onBackClick() {
        composeNavigator.navigateBack()
    }

    fun clearValidationError() {
        changeState {
            copy(
                validationError = null,
                importNotice = null
            )
        }
    }

    fun showSaveTrainingDialog() {
        changeState { copy(showSaveDialog = true) }
    }

    fun hideSaveTrainingDialog() {
        changeState { copy(showSaveDialog = false) }
    }

    fun importTrainingBatch(handles: List<String>) {
        if (handles.isEmpty()) return
        viewModelScope.launch {
            val fileContents = trainingFileReader.readFiles(handles)
            val sources = fileContents.map { file -> TrainingImportParsers.detect(file.bytes)?.source }
            val importSource = sources.filterNotNull().distinct().singleOrNull()
            val error = when {
                fileContents.size < handles.size -> UiText.of(Res.string.add_training_import_read_error)
                importSource == null -> UiText.of(Res.string.add_training_import_unsupported_format)
                sources.size > 1 -> UiText.of(Res.string.add_training_import_mixed_sources)
                else -> null
            }
            if (error != null) {
                changeState { copy(importNotice = error) }
                return@launch
            }
            val parser = TrainingImportParsers.bySource(checkNotNull(importSource))
            fileContents.forEach { file ->
                runCatching {
                    parser.parse(
                        bytes = file.bytes,
                        fileName = file.fileName
                    )
                }.getOrNull()?.let { parsed -> applyParsedImport(parsed) } ?: run {
                    changeState { copy(importNotice = UiText.of(Res.string.add_training_import_parse_error)) }
                    return@launch
                }
            }
        }
    }

    private fun parseIntFromText(text: String): Int? = text.filter { it.isDigit() }.toIntOrNull()

    private fun parseLactateFromText(text: String): Double? {
        val value = text.filter { it.isDigit() || it == ',' || it == '.' }
            .replace(',', '.')
            .toDoubleOrNull()
            ?: return null
        return (value * 10).roundToInt() / 10.0
    }

    private suspend fun applyParsedImport(parsed: ParsedTrainingImport) {
        val newPieces = parsed.intervals.map { interval ->
            AddPieceDraft(
                distanceMeters = interval.distanceMeters,
                timeMillis = interval.timeMillis,
                strokeRate = interval.strokeRate,
                avgHeartRate = interval.avgHeartRate,
                maxHeartRate = interval.maxHeartRate,
                recoveryHeartRate = interval.recoveryHeartRate,
                distanceText = interval.distanceMeters.toString(),
                timeText = RowingTimeFormat.formatDuration(interval.timeMillis),
                strokeRateText = formatTenths(value = interval.strokeRate),
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
                    dateFormatted = TimeUtils.formatDate(trainingDateMillis),
                    crewAthletes = listOf(AddCrewAthlete(rowerId = parsedAthleteId)),
                    pieces = newPieces.ifEmpty { listOf(AddPieceDraft()) },
                    trainingType = parsed.source.impliedTrainingType(),
                    pendingRawCsvs = listOf(parsed.csvText),
                    pendingImportSource = parsed.source,
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
                    pendingRawCsvs = pendingRawCsvs + parsed.csvText,
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
    ): UiText {
        if (serialNotFound && deviceSerial != null) {
            return UiText.of(
                Res.string.add_training_import_not_found_by_serial,
                deviceSerial
            )
        }
        return if (fileCount > 1) {
            UiText.of(
                Res.string.add_training_import_multi_success,
                fileCount,
                pieceCount
            )
        } else {
            UiText.of(Res.string.add_training_import_success)
        }
    }

    private fun toTrainingDateMillis(startTimeMillis: Long): Long {
        val timezone = TimeZone.currentSystemDefault()
        return Instant.fromEpochMilliseconds(startTimeMillis)
            .toLocalDateTime(timeZone = timezone)
            .date
            .atTime(
                hour = 12,
                minute = 0
            )
            .toInstant(timeZone = timezone)
            .toEpochMilliseconds()
    }

    private fun toTrainingDateMillis(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Long = LocalDateTime(
        year = year,
        monthNumber = month,
        dayOfMonth = dayOfMonth,
        hour = 12,
        minute = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    private fun validate(): UiText? = validateCrewSize() ?: validateRowerIds() ?: validatePieces()

    private fun validateCrewSize(): UiText? = when {
        state.crewAthletes.isEmpty() -> UiText.of(Res.string.add_training_validation_need_athlete)
        state.crewAthletes.size > AddTrainingState.MAX_CREW_SIZE -> UiText.of(
            Res.string.add_training_crew_max_size,
            AddTrainingState.MAX_CREW_SIZE
        )

        else -> null
    }

    private fun validateRowerIds(): UiText? {
        val usedAthletes = mutableSetOf<Long>()
        for (athlete in state.crewAthletes) {
            if (athlete.rowerId <= 0) {
                return UiText.of(Res.string.add_training_validation_select_athlete)
            }
            if (!usedAthletes.add(athlete.rowerId)) {
                return UiText.of(Res.string.add_training_validation_athlete_duplicate)
            }
        }
        return null
    }

    private fun validatePieces(): UiText? {
        if (state.pieces.isEmpty()) {
            return UiText.of(Res.string.add_training_validation_need_piece)
        }
        for (piece in state.pieces) {
            val time = RowingTimeFormat.parseTimeInput(input = piece.timeText) ?: piece.timeMillis
            val rate = RowingTimeFormat.parseStrokeRate(piece.strokeRateText) ?: piece.strokeRate
            when {
                piece.distanceMeters <= 0 ->
                    return UiText.of(Res.string.add_training_validation_piece_distance)

                time <= 0 -> return UiText.of(Res.string.add_training_validation_piece_time)

                rate <= 0 -> return UiText.of(Res.string.add_training_validation_piece_stroke_rate)
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
                pieces = pieces.map { piece -> if (piece.localId == pieceLocalId) transform(piece) else piece },
                validationError = null
            )
        }
    }
}
