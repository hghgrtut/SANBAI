package by.rowing.sanbaiteam.training.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingimport.FIT_FILE_EXTENSION
import by.rowing.sanbaiteam.training.data.trainingimport.ParsedTrainingImport
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingFileReader
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportContainer
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportParsers
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.add_training_import_mixed_sources
import by.rowing.sanbaiteam.training.resources.add_training_import_mixed_types
import by.rowing.sanbaiteam.training.resources.add_training_import_multi_success
import by.rowing.sanbaiteam.training.resources.add_training_import_not_found_by_serial
import by.rowing.sanbaiteam.training.resources.add_training_import_parse_error
import by.rowing.sanbaiteam.training.resources.add_training_import_read_error
import by.rowing.sanbaiteam.training.resources.add_training_import_ready
import by.rowing.sanbaiteam.training.resources.add_training_import_success
import by.rowing.sanbaiteam.training.resources.add_training_import_unsupported_format
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

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
), NavigateBackAware {

    private var pendingInitialImportUri: String? = importUri

    init {
        viewModelScope.launch {
            athleteRepository.allAthletes.collectLatest { athletes ->
                changeState {
                    copy(
                        rowersCatalog = athletes,
                        crewAthletes = athletes.singleOrNull()?.id?.let { theOnlyRowerId ->
                            listOfNotNull(crewAthletes.firstOrNull()?.copy(rowerId = theOnlyRowerId))
                        } ?: crewAthletes,
                    )
                }
            }
        }
        if (!importUri.isNullOrBlank()) {
            changeState { copy(importNotice = UiText.of(Res.string.add_training_import_ready)) }
        }
    }

    override fun navigateBack() {
        when {
            state.showSaveDialog || state.showExitDialog -> changeState {
                copy(
                    showSaveDialog = false,
                    showExitDialog = false
                )
            }

            state.isDirty -> changeState { copy(showExitDialog = true) }
            else -> composeNavigator.navigateBack()
        }
    }

    fun confirmExit() {
        changeState { copy(showExitDialog = false) }
        composeNavigator.navigateBack()
    }

    fun dismissExitDialog() {
        changeState { copy(showExitDialog = false) }
    }

    fun dismissImportNotice() {
        changeState { copy(importNotice = null) }
    }

    fun consumeScrollTarget() {
        changeState { copy(scrollTarget = null) }
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
                dateFormatted = TimeUtils.formatDate(millis),
                isDirty = true,
            )
        }
    }

    fun changeTrainingType(trainingType: TrainingPieceType) {
        changeState {
            copy(
                trainingType = trainingType,
                validationError = null,
                isDirty = true,
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
                validationError = null,
                athleteError = null,
                isDirty = true,
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
                    sourceFileIndex = null,
                    sourceIntervalIndex = null,
                )
            } else {
                AddPieceDraft()
            }
            copy(
                pieces = pieces + newPiece,
                scrollTarget = AddTrainingScrollTarget.Piece(newPiece.localId),
                validationError = null,
                isDirty = true,
            )
        }
    }

    fun removePiece(pieceLocalId: Long) {
        if (state.pieces.size > 1) {
            changeState {
                copy(
                    pieces = pieces.filterNot { it.localId == pieceLocalId },
                    pieceErrors = pieceErrors - pieceLocalId,
                    validationError = null,
                    isDirty = true,
                )
            }
        }
    }

    fun saveTraining() {
        val validation = validateAddTraining(
            crewAthletes = state.crewAthletes,
            pieces = state.pieces,
        )
        if (!validation.isValid) {
            changeState {
                copy(
                    validationError = validation.generalError,
                    athleteError = validation.athleteError,
                    pieceErrors = validation.pieceErrors,
                    scrollTarget = validation.athleteError?.let { AddTrainingScrollTarget.Athlete }
                        ?: validation.firstInvalidPieceId?.let { pieceId ->
                            AddTrainingScrollTarget.Piece(pieceLocalId = pieceId)
                        },
                )
            }
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
                        sourceFileIndex = piece.sourceFileIndex,
                        sourceIntervalIndex = piece.sourceIntervalIndex,
                    )
                }
            }
            val trainingId = trainingRepository.saveTraining(
                dateMillis = state.dateMillis,
                type = state.trainingType,
                pieces = piecesToSave,
            )
            if (state.pendingRawFiles.isNotEmpty()) {
                val importSource = state.pendingImportSource ?: TrainingImportSource.SPEED_COACH
                val packed = TrainingImportContainer.pack(state.pendingRawFiles)
                val fileExtension = if (state.pendingRawFiles.size == 1) {
                    state.pendingFileExtension ?: FIT_FILE_EXTENSION
                } else {
                    TrainingImportContainer.EXTENSION
                }
                val relativePath = trainingImportStorage.saveImportFile(
                    trainingId = trainingId,
                    source = importSource,
                    serial = state.pendingSourceSerial,
                    fileExtension = fileExtension,
                    bytes = packed
                )
                trainingRepository.updateTrainingImportMetadata(
                    trainingId = trainingId,
                    relativePath = relativePath,
                )
            }
            withContext(Dispatchers.Main.immediate) { composeNavigator.navigateBack() }
        }
    }

    fun clearValidationError() {
        changeState {
            copy(
                validationError = null,
                athleteError = null,
                pieceErrors = emptyMap(),
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
                sources.any { it == null } -> UiText.of(Res.string.add_training_import_unsupported_format)
                importSource == null -> UiText.of(Res.string.add_training_import_mixed_sources)
                else -> null
            }
            if (error != null) {
                changeState { copy(importNotice = error) }
                return@launch
            }
            val parser = TrainingImportParsers.bySource(checkNotNull(importSource))
            val parsedImports = mutableListOf<ParsedTrainingImport>()
            fileContents.forEach { file ->
                TrainingImportContainer.unpackOrSingle(file.bytes).forEach { part ->
                    val parsed = runCatching {
                        parser.parse(
                            bytes = part,
                            fileName = file.fileName
                        )
                    }.getOrNull() ?: run {
                        changeState {
                            copy(importNotice = UiText.of(Res.string.add_training_import_parse_error))
                        }
                        return@launch
                    }
                    parsedImports += parsed
                }
            }
            if (parsedImports.map { it.trainingType }.toSet().size > 1) {
                changeState { copy(importNotice = UiText.of(Res.string.add_training_import_mixed_types)) }
                return@launch
            }
            var fileIndex = state.pendingRawFiles.size
            parsedImports.forEach { parsed ->
                applyParsedImport(
                    parsed = parsed,
                    fileIndex = fileIndex
                )
                fileIndex++
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

    private suspend fun applyParsedImport(
        parsed: ParsedTrainingImport,
        fileIndex: Int,
    ) {
        val pendingTrainingType = state.pendingImportTrainingType
        if (pendingTrainingType != null && pendingTrainingType != parsed.trainingType) {
            changeState { copy(importNotice = UiText.of(Res.string.add_training_import_mixed_types)) }
            return
        }
        val newPieces = parsed.intervals.mapIndexed { intervalIndex, interval ->
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
                sourceFileIndex = fileIndex,
                sourceIntervalIndex = intervalIndex,
            )
        }
        val isFirstImport = state.pendingRawFiles.isEmpty()

        if (isFirstImport) {
            val parsedAthleteId = resolveImportedAthleteId(parsed = parsed)
            val trainingDateMillis = parsed.startTimeMillis?.let(::toTrainingDateMillis) ?: state.dateMillis
            changeState {
                copy(
                    dateMillis = trainingDateMillis,
                    dateFormatted = TimeUtils.formatDate(trainingDateMillis),
                    crewAthletes = listOf(AddCrewAthlete(rowerId = parsedAthleteId)),
                    pieces = newPieces.ifEmpty { listOf(AddPieceDraft()) },
                    trainingType = parsed.trainingType,
                    pendingImportTrainingType = parsed.trainingType,
                    pendingRawFiles = listOf(parsed.rawFile),
                    pendingFileExtension = parsed.fileExtension,
                    pendingImportSource = parsed.source,
                    pendingSourceSerial = parsed.deviceSerial,
                    importNotice = buildImportNotice(
                        fileCount = 1,
                        pieceCount = newPieces.size,
                        serialNotFound = parsedAthleteId == 0L && !parsed.deviceSerial.isNullOrBlank(),
                        deviceSerial = parsed.deviceSerial,
                    ),
                    validationError = null,
                    athleteError = null,
                    pieceErrors = emptyMap(),
                    scrollTarget = null,
                    isDirty = true,
                )
            }
        } else {
            val fileCount = state.pendingRawFiles.size + 1
            val pieceCount = state.pieces.size + newPieces.size
            changeState {
                copy(
                    pieces = pieces + newPieces,
                    pendingRawFiles = pendingRawFiles + parsed.rawFile,
                    importNotice = buildImportNotice(
                        fileCount = fileCount,
                        pieceCount = pieceCount,
                        serialNotFound = false,
                        deviceSerial = null,
                    ),
                    validationError = null,
                    athleteError = null,
                    pieceErrors = emptyMap(),
                    isDirty = true,
                )
            }
        }
    }

    private suspend fun resolveImportedAthleteId(parsed: ParsedTrainingImport): Long =
        parsed.deviceSerial?.let { athleteRepository.findAthleteIdBySpeedCoachSerial(it) } ?: run {
            val catalog = state.rowersCatalog
            if (catalog.isNotEmpty()) {
                catalog.singleOrNull()?.id
            } else {
                athleteRepository.getAllAthletesOnce().singleOrNull()?.id
            }
        } ?: 0L

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
        month = month,
        day = dayOfMonth,
        hour = 12,
        minute = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    private fun updatePiece(
        pieceLocalId: Long,
        transform: (AddPieceDraft) -> AddPieceDraft
    ) {
        changeState {
            copy(
                pieces = pieces.map { piece -> if (piece.localId == pieceLocalId) transform(piece) else piece },
                pieceErrors = pieceErrors - pieceLocalId,
                validationError = null,
                isDirty = true,
            )
        }
    }
}