package by.rowing.sanbaiteam.backup.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteWeightDao
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.backup.data.model.BackupAthlete
import by.rowing.sanbaiteam.backup.data.model.BackupPiece
import by.rowing.sanbaiteam.backup.data.model.BackupTraining
import by.rowing.sanbaiteam.backup.data.model.BackupWeight
import by.rowing.sanbaiteam.backup.data.model.SanbaiBackup
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.backup.domain.model.BackupImportResult
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.trainingimport.FIT_FILE_EXTENSION
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64

@AllowDetektPublic
class BackupRepository(
    private val trainingDao: TrainingDao,
    private val athletesRepository: AthletesRepository,
    private val athleteWeightDao: AthleteWeightDao,
    private val importStorage: TrainingImportStorage,
    private val backupSerializer: BackupSerializer,
    private val nowMillis: () -> Long = ::nowInMillis,
) {

    suspend fun exportUnifiedBackup(
        fromMillis: Long,
        toMillis: Long
    ): SanbaiBackup = withContext(Dispatchers.Default) {
        val (rangeStart, rangeEnd) = normalizeDayRange(
            fromMillis = fromMillis,
            toMillis = toMillis
        )
        val trainings = trainingDao.getTrainingEntitiesByDateRange(
            fromMillis = rangeStart,
            toMillis = rangeEnd,
        )
        val piecesByTrainingId = trainingDao.getPiecesForTrainings(trainingIds = trainings.map { it.id })
            .groupBy { it.trainingId }
        val weightsInPeriod = athleteWeightDao.getByDateRange(
            fromMillis = rangeStart,
            toMillis = rangeEnd,
        )

        buildBackup(
            trainings = trainings,
            piecesByTrainingId = piecesByTrainingId,
            athleteIds = buildSet {
                piecesByTrainingId.values.flatten().forEach { add(it.athleteId) }
                weightsInPeriod.forEach { add(it.athleteId) }
            },
            weightsByAthleteId = weightsInPeriod.groupBy { it.athleteId },
            fromMillis = rangeStart,
            toMillis = rangeEnd,
        )
    }

    suspend fun exportSelectedTrainings(trainingIds: Set<Long>): SanbaiBackup =
        withContext(Dispatchers.Default) {
            val trainings = trainingDao.getTrainingEntitiesByIds(trainingIds.toList())
            val piecesByTrainingId = trainingDao.getPiecesForTrainings(trainingIds = trainings.map { it.id })
                .groupBy { it.trainingId }
            val referencedAthleteIds = piecesByTrainingId.values.flatten().map { it.athleteId }.toSet()
            val (fromMillis, toMillis) = trainingsDateRangeOrNull(trainings) ?: (null to null)
            val weightsByAthleteId = if (fromMillis != null && toMillis != null) {
                athleteWeightDao.getByDateRange(
                    fromMillis = fromMillis,
                    toMillis = toMillis
                ).filter { it.athleteId in referencedAthleteIds }
                    .groupBy { it.athleteId }
            } else {
                emptyMap()
            }

            buildBackup(
                trainings = trainings,
                piecesByTrainingId = piecesByTrainingId,
                athleteIds = referencedAthleteIds,
                weightsByAthleteId = weightsByAthleteId,
                fromMillis = fromMillis,
                toMillis = toMillis,
            )
        }

    suspend fun exportSelectedAthletes(athleteIds: Set<Long>): SanbaiBackup =
        withContext(Dispatchers.Default) {
            val athletesById = athletesRepository.getAllAthletesOnce().associateBy { it.id }
            val backupAthletes = athleteIds.mapNotNull { athleteId ->
                val athlete = athletesById[athleteId] ?: return@mapNotNull null
                val weights = athletesRepository.getWeights(athleteId = athleteId)
                    .sortedBy { it.dateMillis }
                    .map { it.toBackupWeight() }
                athlete.toBackupAthlete(weights = weights)
            }

            SanbaiBackup(
                exportedAtMillis = nowMillis(),
                fromMillis = null,
                toMillis = null,
                athletes = backupAthletes,
                trainings = emptyList(),
            )
        }

    suspend fun import(text: String): BackupImportResult = withContext(Dispatchers.Default) {
        val backup = backupSerializer.decode(text)
        importBackup(backup = backup)
    }

    suspend fun importBackup(backup: SanbaiBackup): BackupImportResult = withContext(Dispatchers.Default) {
        if (backup.kind != SanbaiBackup.KIND || backup.formatVersion != SanbaiBackup.FORMAT_VERSION) {
            throw IllegalArgumentException(
                "Unsupported backup format: kind=${backup.kind}, formatVersion=${backup.formatVersion}"
            )
        }

        val existingAthletes = athletesRepository.getAllAthletesOnce().toMutableList()
        val athleteIdByIndex = mutableMapOf<Int, Long>()
        var athletesCreated = 0
        var athletesUpdated = 0
        var weightsImported = 0

        backup.athletes.forEachIndexed { index, backupAthlete ->
            val name = backupAthlete.name.trim()
            if (name.isEmpty() || backupAthlete.dateOfBirthMillis <= 0) {
                return@forEachIndexed
            }
            val dateOfBirthMillis = TimeUtils.startOfDayMillis(timeInMillis = backupAthlete.dateOfBirthMillis)
            val matched = findAthleteMatch(
                athletes = existingAthletes,
                name = name,
                dateOfBirthMillis = dateOfBirthMillis,
            )

            val athleteId = if (matched == null) {
                val newAthlete = AthleteEntity(
                    name = name,
                    dateOfBirth = dateOfBirthMillis,
                    isMale = backupAthlete.isMale,
                    speedCoachSerial = null,
                )
                val newId = athletesRepository.addAthlete(athlete = newAthlete)
                existingAthletes += newAthlete.copy(id = newId)
                athletesCreated++
                newId
            } else {
                athletesUpdated++
                matched.id
            }
            athleteIdByIndex[index] = athleteId

            val weightsToWrite = sanitizeWeights(
                athleteId = athleteId,
                weights = backupAthlete.weights,
            )
            athletesRepository.upsertWeights(weights = weightsToWrite)
            weightsImported += weightsToWrite.size
        }

        val trainingImport = importTrainings(
            trainings = backup.trainings,
            athleteIdByIndex = athleteIdByIndex,
        )

        BackupImportResult(
            trainingsImported = trainingImport.imported,
            trainingsSkippedAsDuplicates = trainingImport.skippedAsDuplicates,
            athletesCreated = athletesCreated,
            athletesUpdated = athletesUpdated,
            weightsImported = weightsImported,
        )
    }

    private suspend fun buildBackup(
        trainings: List<TrainingEntity>,
        piecesByTrainingId: Map<Long, List<TrainingAthletePieceEntity>>,
        athleteIds: Set<Long>,
        weightsByAthleteId: Map<Long, List<AthleteWeightEntity>>,
        fromMillis: Long?,
        toMillis: Long?,
    ): SanbaiBackup {
        val athletesById = athletesRepository.getAllAthletesOnce().associateBy { it.id }
        val athleteIndexById = LinkedHashMap<Long, Int>()
        val backupAthletes = mutableListOf<BackupAthlete>()

        fun ensureAthlete(athleteId: Long) {
            if (athleteId in athleteIndexById) {
                return
            }
            val athlete = athletesById[athleteId] ?: return
            athleteIndexById[athleteId] = backupAthletes.size
            val weights = weightsByAthleteId[athleteId].orEmpty().sortedBy { it.dateMillis }.map { it.toBackupWeight() }
            backupAthletes += athlete.toBackupAthlete(weights = weights)
        }

        referencedAthleteIdsSorted(
            athleteIds = athleteIds,
            athletesById = athletesById
        ).forEach(::ensureAthlete)

        val backupTrainings = trainings.map { training ->
            val pieces = piecesByTrainingId[training.id].orEmpty()
            pieces.map { it.athleteId }.distinct().forEach(::ensureAthlete)
            BackupTraining(
                dateMillis = training.dateMillis,
                type = training.type.name,
                source = training.sourceFileRelativePath?.let(::sourceByPath)?.name,
                sourceFileBase64 = training.sourceFileRelativePath
                    ?.let { importStorage.readImportFile(it) }
                    ?.let { Base64.encode(it) },
                sourceFileExtension = training.sourceFileRelativePath
                    ?.substringAfterLast('.', "")
                    ?.takeIf { it.isNotBlank() },
                pieces = pieces.mapNotNull { piece ->
                    val athleteIndex = athleteIndexById[piece.athleteId] ?: return@mapNotNull null
                    piece.toBackupPiece(athleteIndex = athleteIndex)
                },
            )
        }

        return SanbaiBackup(
            exportedAtMillis = nowMillis(),
            fromMillis = fromMillis,
            toMillis = toMillis,
            athletes = backupAthletes,
            trainings = backupTrainings,
        )
    }

    private suspend fun importTrainings(
        trainings: List<BackupTraining>,
        athleteIdByIndex: Map<Int, Long>,
    ): TrainingImportCounts {
        val (candidateStart, candidateEnd) = trainingsDateRangeOrNull(
            trainings = trainings.map { TrainingDateHolder(it.dateMillis) }
        ) ?: return TrainingImportCounts()
        val existingTrainings = trainingDao.getTrainingEntitiesByDateRange(
            fromMillis = candidateStart,
            toMillis = candidateEnd,
        )
        val existingPiecesByTrainingId = trainingDao.getPiecesForTrainings(existingTrainings.map { it.id })
            .groupBy { it.trainingId }
        val existingSignatures = existingTrainings.map { training ->
            buildExistingSignature(
                training = training,
                pieces = existingPiecesByTrainingId[training.id].orEmpty(),
            )
        }.toMutableList()

        var imported = 0
        var skippedAsDuplicates = 0
        for (training in trainings) {
            val resolvedPieces = training.pieces.mapNotNull { piece ->
                val athleteId = athleteIdByIndex[piece.athleteIndex] ?: return@mapNotNull null
                ResolvedBackupPiece(
                    athleteId = athleteId,
                    order = piece.order,
                    distanceMeters = piece.distanceMeters,
                    timeMillis = piece.timeMillis,
                    strokeRate = piece.strokeRate,
                    avgHeartRate = piece.avgHeartRate,
                    maxHeartRate = piece.maxHeartRate,
                    recoveryHeartRate = piece.recoveryHeartRate,
                    lactate = piece.lactate,
                    sourceFileIndex = piece.sourceFileIndex,
                    sourceIntervalIndex = piece.sourceIntervalIndex,
                )
            }.ifEmpty { continue }

            val type = runCatching { TrainingPieceType.valueOf(training.type) }
                .getOrDefault(TrainingPieceType.SINGLE)
            val sourceBytes = training.sourceFileBase64?.let { runCatching { Base64.decode(it) }.getOrNull() }
            val signature = TrainingSignature(
                type = type,
                dayStartMillis = TimeUtils.startOfDayMillis(timeInMillis = training.dateMillis),
                pieces = resolvedPieces.map { it.signature() }.sorted(),
                sourceBytes = sourceBytes,
            )
            if (existingSignatures.any { it.isDuplicateOf(signature) }) {
                skippedAsDuplicates++
                continue
            }

            val newTrainingId = trainingDao.saveTrainingWithPieces(
                training = TrainingEntity(
                    dateMillis = training.dateMillis,
                    type = type,
                ),
                pieces = resolvedPieces.map { it.toEntity(trainingId = 0) },
            )
            restoreImportFile(
                trainingId = newTrainingId,
                training = training,
                sourceBytes = sourceBytes,
            )?.let { relativePath ->
                trainingDao.updateImportMetadata(
                    trainingId = newTrainingId,
                    relativePath = relativePath,
                )
            }
            existingSignatures += signature
            imported++
        }
        return TrainingImportCounts(
            imported = imported,
            skippedAsDuplicates = skippedAsDuplicates
        )
    }

    private fun buildExistingSignature(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>,
    ): TrainingSignature = TrainingSignature(
        type = training.type,
        dayStartMillis = TimeUtils.startOfDayMillis(timeInMillis = training.dateMillis),
        pieces = pieces.map {
            PieceSignature(
                athleteId = it.athleteId,
                order = it.order,
                distanceMeters = it.distanceMeters,
                timeMillis = it.timeMillis,
                strokeRate = it.strokeRate,
                avgHeartRate = it.averageHeartRate,
                maxHeartRate = it.maxHeartRate,
                recoveryHeartRate = it.recoveryHeartRate,
                lactate = it.lactate,
                sourceFileIndex = it.sourceFileIndex,
                sourceIntervalIndex = it.sourceIntervalIndex,
            )
        }.sorted(),
        sourceBytes = training.sourceFileRelativePath?.let { importStorage.readImportFile(it) },
    )

    private fun restoreImportFile(
        trainingId: Long,
        training: BackupTraining,
        sourceBytes: ByteArray?,
    ): String? = importStorage.saveImportFile(
        bytes = sourceBytes ?: return null,
        trainingId = trainingId,
        source = sourceOf(training = training),
        serial = null,
        fileExtension = training.sourceFileExtension?.takeIf { it.isNotBlank() } ?: FIT_FILE_EXTENSION,
    )

    private fun sanitizeWeights(
        athleteId: Long,
        weights: List<BackupWeight>,
    ): List<AthleteWeightEntity> {
        val todayStartMillis = TimeUtils.startOfDayMillis(timeInMillis = nowMillis())
        return weights.mapNotNull { weight ->
            val dateMillis = TimeUtils.startOfDayMillis(timeInMillis = weight.dateMillis)
            AthleteWeightEntity(
                athleteId = athleteId,
                dateMillis = TimeUtils.noonOfDay(timeInMillis = dateMillis),
                weightTenths = weight.weightTenths,
            ).takeIf {
                dateMillis <= todayStartMillis &&
                        weight.weightTenths in MIN_WEIGHT_TENTHS..MAX_WEIGHT_TENTHS
            }
        }.associateBy { it.dateMillis }.values.toList()
    }

    private fun sourceOf(training: BackupTraining): TrainingImportSource = training.source?.let { name ->
        runCatching { TrainingImportSource.valueOf(name) }.getOrNull()
    } ?: when (runCatching { TrainingPieceType.valueOf(training.type) }.getOrNull()) {
        TrainingPieceType.CONCEPT_ROW_ERG,
        TrainingPieceType.CONCEPT_BIKE_ERG -> TrainingImportSource.CONCEPT2_FIT

        TrainingPieceType.RP_3 -> TrainingImportSource.RP3_CSV
        else -> TrainingImportSource.SPEED_COACH
    }

    private fun sourceByPath(relativePath: String): TrainingImportSource =
        TrainingImportSource.entries.firstOrNull { relativePath.startsWith(it.storageFolder) }
            ?: TrainingImportSource.SPEED_COACH

    private fun findAthleteMatch(
        athletes: List<AthleteEntity>,
        name: String,
        dateOfBirthMillis: Long,
    ): AthleteEntity? {
        val normalizedName = normalizeName(name = name)
        return athletes.firstOrNull { athlete ->
            normalizeName(name = athlete.name).equals(normalizedName, ignoreCase = true) &&
                    TimeUtils.isSameCalendarDay(
                        firstMillis = athlete.dateOfBirth,
                        secondMillis = dateOfBirthMillis,
                    )
        }
    }

    private fun referencedAthleteIdsSorted(
        athleteIds: Set<Long>,
        athletesById: Map<Long, AthleteEntity>,
    ): List<Long> = athleteIds.sortedWith(
        comparator = compareBy<Long> { athletesById[it]?.name.orEmpty() }
            .thenBy { athletesById[it]?.dateOfBirth ?: Long.MAX_VALUE }
            .thenBy { it }
    )

    private fun normalizeDayRange(
        fromMillis: Long,
        toMillis: Long
    ): Pair<Long, Long> {
        val fromDayStart = TimeUtils.startOfDayMillis(timeInMillis = minOf(fromMillis, toMillis))
        val toDayStart = TimeUtils.startOfDayMillis(timeInMillis = maxOf(fromMillis, toMillis))
        return fromDayStart to (toDayStart + TimeUtils.MILLIS_PER_DAY - 1)
    }

    private fun trainingsDateRangeOrNull(trainings: List<TrainingEntity>): Pair<Long, Long>? {
        val minDate = trainings.minOfOrNull { it.dateMillis } ?: return null
        val maxDate = trainings.maxOfOrNull { it.dateMillis } ?: return null
        return normalizeDayRange(
            fromMillis = minDate,
            toMillis = maxDate
        )
    }

    private fun trainingsDateRangeOrNull(trainings: List<TrainingDateHolder>): Pair<Long, Long>? {
        val minDate = trainings.minOfOrNull { it.dateMillis } ?: return null
        val maxDate = trainings.maxOfOrNull { it.dateMillis } ?: return null
        return normalizeDayRange(
            fromMillis = minDate,
            toMillis = maxDate
        )
    }

    private fun normalizeName(name: String): String = name.trim().split(WHITESPACE_REGEX).joinToString(separator = " ")

    private fun AthleteEntity.toBackupAthlete(weights: List<BackupWeight>): BackupAthlete = BackupAthlete(
        name = name,
        dateOfBirthMillis = dateOfBirth,
        isMale = isMale,
        weights = weights,
    )

    private fun AthleteWeightEntity.toBackupWeight(): BackupWeight = BackupWeight(
        dateMillis = dateMillis,
        weightTenths = weightTenths,
    )

    private fun TrainingAthletePieceEntity.toBackupPiece(athleteIndex: Int): BackupPiece = BackupPiece(
        athleteIndex = athleteIndex,
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = strokeRate,
        avgHeartRate = averageHeartRate,
        maxHeartRate = maxHeartRate,
        recoveryHeartRate = recoveryHeartRate,
        lactate = lactate,
        sourceFileIndex = sourceFileIndex,
        sourceIntervalIndex = sourceIntervalIndex,
    )

    companion object {

        private const val MIN_WEIGHT_TENTHS = 200
        private const val MAX_WEIGHT_TENTHS = 3000
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    private data class TrainingDateHolder(val dateMillis: Long)

    private data class TrainingImportCounts(
        val imported: Int = 0,
        val skippedAsDuplicates: Int = 0,
    )

    private data class ResolvedBackupPiece(
        val athleteId: Long,
        val order: Int,
        val distanceMeters: Int,
        val timeMillis: Long,
        val strokeRate: Double,
        val avgHeartRate: Int?,
        val maxHeartRate: Int?,
        val recoveryHeartRate: Int?,
        val lactate: Double?,
        val sourceFileIndex: Int?,
        val sourceIntervalIndex: Int?,
    ) {

        fun signature(): PieceSignature = PieceSignature(
            athleteId = athleteId,
            order = order,
            distanceMeters = distanceMeters,
            timeMillis = timeMillis,
            strokeRate = strokeRate,
            avgHeartRate = avgHeartRate,
            maxHeartRate = maxHeartRate,
            recoveryHeartRate = recoveryHeartRate,
            lactate = lactate,
            sourceFileIndex = sourceFileIndex,
            sourceIntervalIndex = sourceIntervalIndex,
        )

        fun toEntity(trainingId: Long): TrainingAthletePieceEntity = TrainingAthletePieceEntity(
            trainingId = trainingId,
            athleteId = athleteId,
            order = order,
            distanceMeters = distanceMeters,
            timeMillis = timeMillis,
            strokeRate = strokeRate,
            averageHeartRate = avgHeartRate,
            maxHeartRate = maxHeartRate,
            recoveryHeartRate = recoveryHeartRate,
            lactate = lactate,
            sourceFileIndex = sourceFileIndex,
            sourceIntervalIndex = sourceIntervalIndex,
        )
    }

    private data class PieceSignature(
        val athleteId: Long,
        val order: Int,
        val distanceMeters: Int,
        val timeMillis: Long,
        val strokeRate: Double,
        val avgHeartRate: Int?,
        val maxHeartRate: Int?,
        val recoveryHeartRate: Int?,
        val lactate: Double?,
        val sourceFileIndex: Int?,
        val sourceIntervalIndex: Int?,
    ) : Comparable<PieceSignature> {

        override fun compareTo(other: PieceSignature): Int = compareValuesBy(
            this,
            other,
            { it.athleteId },
            { it.order },
            { it.distanceMeters },
            { it.timeMillis },
            { it.strokeRate },
            { it.avgHeartRate ?: Int.MIN_VALUE },
            { it.maxHeartRate ?: Int.MIN_VALUE },
            { it.recoveryHeartRate ?: Int.MIN_VALUE },
            { it.lactate ?: Double.NEGATIVE_INFINITY },
            { it.sourceFileIndex ?: Int.MIN_VALUE },
            { it.sourceIntervalIndex ?: Int.MIN_VALUE },
        )
    }

    private data class TrainingSignature(
        val type: TrainingPieceType,
        val dayStartMillis: Long,
        val pieces: List<PieceSignature>,
        val sourceBytes: ByteArray?,
    ) {

        fun isDuplicateOf(other: TrainingSignature): Boolean =
            type == other.type &&
                    dayStartMillis == other.dayStartMillis &&
                    pieces == other.pieces &&
                    bytesEqual(sourceBytes, other.sourceBytes)

        private fun bytesEqual(
            first: ByteArray?,
            second: ByteArray?
        ): Boolean = when {
            first == null && second == null -> true
            first == null || second == null -> false
            else -> first.contentEquals(second)
        }
    }
}