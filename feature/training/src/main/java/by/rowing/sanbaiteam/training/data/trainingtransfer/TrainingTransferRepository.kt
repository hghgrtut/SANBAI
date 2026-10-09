package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.models.TrainingTransferResult
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportSource
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

internal class TrainingTransferRepository(
    private val trainingDao: TrainingDao,
    private val athletesRepository: AthletesRepository,
    private val importStorage: TrainingImportStorage,
) {

    suspend fun exportAllBackup(): TrainingBackup = withContext(Dispatchers.IO) {
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
                            dateOfBirthMillis = athlete?.dateOfBirth?.time ?: 0L,
                            isMale = athlete?.isMale ?: false,
                        )
                    }
                }

                BackupTraining(
                    dateMillis = training.dateMillis,
                    type = training.type.name,
                    sourceDeviceSerial = training.sourceDeviceSerial,
                    sourceSessionName = training.sourceSessionName,
                    sourceCsvContent = training.sourceCsvRelativePath?.let { importStorage.readCsv(it) },
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
                        )
                    }
                )
            }
        )
    }

    suspend fun importBackup(backup: TrainingBackup): TrainingTransferResult = withContext(Dispatchers.IO) {
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
                        dateOfBirth = Date(backupAthlete.dateOfBirthMillis),
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
                )
            }
            if (pieces.isEmpty()) continue

            val newTrainingId = trainingDao.saveTrainingWithPieces(
                training = TrainingEntity(
                    dateMillis = training.dateMillis,
                    type = runCatching { TrainingPieceType.valueOf(training.type) }
                        .getOrDefault(TrainingPieceType.SINGLE),
                    sourceDeviceSerial = training.sourceDeviceSerial,
                    sourceSessionName = training.sourceSessionName,
                ),
                pieces = pieces
            )

            if (training.sourceCsvContent != null) {
                val csvRelativePath = importStorage.saveCsv(
                    trainingId = newTrainingId,
                    source = TrainingImportSource.byCsvContent(training.sourceCsvContent),
                    serial = training.sourceDeviceSerial,
                    csvText = training.sourceCsvContent,
                )
                trainingDao.updateImportMetadata(
                    trainingId = newTrainingId,
                    relativePath = csvRelativePath,
                    deviceSerial = training.sourceDeviceSerial,
                    sessionName = training.sourceSessionName,
                )
            }
            trainingsImported++
        }

        TrainingTransferResult(
            trainingsImported = trainingsImported,
            athletesCreated = athletesCreated,
        )
    }
}
