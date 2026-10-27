package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.models.TrainingTransferResult
import by.rowing.sanbaiteam.training.data.trainingimport.FIT_FILE_EXTENSION
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64

internal class TrainingTransferRepository(
    private val trainingDao: TrainingDao,
    private val athletesRepository: AthletesRepository,
    private val importStorage: TrainingImportStorage,
) {

    suspend fun exportAllBackup(): TrainingBackup = withContext(Dispatchers.Default) {
        val athletesById = athletesRepository.getAllAthletesOnce().associateBy { it.id }
        val trainingsWithPieces = trainingDao.getAllTrainingEntities().map { training ->
            training to trainingDao.getPiecesForTraining(training.id)
        }

        val athleteIndexInExportData = LinkedHashMap<Long, Int>()
        val backupAthletes = mutableListOf<BackupAthlete>()

        TrainingBackup(
            athletes = backupAthletes,
            trainings = trainingsWithPieces.map { (training, pieces) ->
                for (athleteId in pieces.map { it.athleteId }.distinct()) {
                    if (athleteId !in athleteIndexInExportData) {
                        // Добавление спортсмена, который ранее не встречался в тренировках, id - порядковый номер
                        athleteIndexInExportData[athleteId] = backupAthletes.size
                        val athlete = athletesById[athleteId]
                        backupAthletes += BackupAthlete(
                            name = athlete?.name.orEmpty(),
                            dateOfBirthMillis = athlete?.dateOfBirth ?: 0L,
                            isMale = athlete?.isMale ?: false,
                        )
                    }
                }

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
                    pieces = pieces.map { piece ->
                        BackupPiece(
                            athleteIndex = athleteIndexInExportData.getValue(piece.athleteId),
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
                    }
                )
            }
        )
    }

    suspend fun importBackup(backup: TrainingBackup): TrainingTransferResult = withContext(Dispatchers.Default) {
        if (backup.formatVersion != TrainingBackup.FORMAT_VERSION) {
            throw IllegalArgumentException(
                "Unsupported backup format version: ${backup.formatVersion}, " +
                        "supports only ${TrainingBackup.FORMAT_VERSION}"
            )
        }

        val existingMatches = matchBackupAthletesToExisting(
            backupAthletes = backup.athletes,
            existing = athletesRepository.getAllAthletesOnce()
        )

        val athleteIdByIndex = mutableMapOf<Int, Long>()
        var athletesCreated = 0
        backup.athletes.forEachIndexed { index, backupAthlete ->
            existingMatches.get(index)?.let { existingId ->
                athleteIdByIndex[index] = existingId
            } ?: run {
                athleteIdByIndex[index] = athletesRepository.addAthlete(
                    AthleteEntity(
                        name = backupAthlete.name,
                        dateOfBirth = backupAthlete.dateOfBirthMillis,
                        isMale = backupAthlete.isMale,
                        speedCoachSerial = null,
                    )
                )
                athletesCreated++
            }
        }

        var trainingsImported = 0
        for (training in backup.trainings) {
            val pieces = training.pieces.mapNotNull { piece ->
                val athleteId = athleteIdByIndex[piece.athleteIndex] ?: return@mapNotNull null
                TrainingAthletePieceEntity(
                    trainingId = 0,
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
            }
            if (pieces.isEmpty()) continue

            val newTrainingId = trainingDao.saveTrainingWithPieces(
                training = TrainingEntity(
                    dateMillis = training.dateMillis,
                    type = runCatching { TrainingPieceType.valueOf(training.type) }
                        .getOrDefault(TrainingPieceType.SINGLE),
                ),
                pieces = pieces
            )

            restoreImportFile(
                trainingId = newTrainingId,
                training = training
            )?.let { relativePath ->
                trainingDao.updateImportMetadata(
                    trainingId = newTrainingId,
                    relativePath = relativePath,
                )
            }
            trainingsImported++
        }

        TrainingTransferResult(
            trainingsImported = trainingsImported,
            athletesCreated = athletesCreated,
        )
    }

    private fun restoreImportFile(
        trainingId: Long,
        training: BackupTraining,
    ): String? = importStorage.saveImportFile(
        bytes = Base64.decode(source = training.sourceFileBase64 ?: return null),
        trainingId = trainingId,
        source = sourceOf(training),
        serial = null,
        fileExtension = training.sourceFileExtension?.takeIf { it.isNotBlank() } ?: FIT_FILE_EXTENSION,
    )

    private fun sourceOf(training: BackupTraining): TrainingImportSource = training.source?.let { name ->
        runCatching { TrainingImportSource.valueOf(name) }.getOrNull()
    } ?: when (runCatching { TrainingPieceType.valueOf(training.type) }.getOrNull()) {
        TrainingPieceType.CONCEPT_ROW_ERG -> TrainingImportSource.CONCEPT2_FIT
        TrainingPieceType.RP_3 -> TrainingImportSource.RP3_CSV
        else -> TrainingImportSource.SPEED_COACH
    }

    private fun sourceByPath(relativePath: String): TrainingImportSource =
        TrainingImportSource.entries.firstOrNull { relativePath.startsWith(it.storageFolder) }
            ?: TrainingImportSource.SPEED_COACH
}