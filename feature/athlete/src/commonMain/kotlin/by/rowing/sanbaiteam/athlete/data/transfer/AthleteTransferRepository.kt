package by.rowing.sanbaiteam.athlete.data.transfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.data.transfer.models.AthleteBackup
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupAthlete
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupPersonalBest
import by.rowing.sanbaiteam.athlete.data.transfer.models.BackupWeight
import by.rowing.sanbaiteam.athlete.domain.transfer.AthleteBackupMerge
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightInput.MAX_WEIGHT_TENTHS
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightInput.MIN_WEIGHT_TENTHS
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AthleteTransferRepository(private val athletesRepository: AthletesRepository) {

    suspend fun exportBackup(athleteIds: Set<Long>): AthleteBackup = withContext(Dispatchers.Default) {
        AthleteBackup(
            kind = AthleteBackup.KIND,
            formatVersion = AthleteBackup.FORMAT_VERSION,
            athletes = athleteIds.mapNotNull { athleteId -> buildBackupAthlete(athleteId = athleteId) },
        )
    }

    suspend fun importBackup(backup: AthleteBackup): AthleteTransferResult = withContext(Dispatchers.Default) {
        if (backup.kind != AthleteBackup.KIND || backup.formatVersion != AthleteBackup.FORMAT_VERSION) {
            throw IllegalArgumentException(
                "Unsupported athlete backup: kind=${backup.kind}, version=${backup.formatVersion}"
            )
        }

        val todayMillis = TimeUtils.startOfDayMillis(timeInMillis = nowInMillis())
        val existingAthletes = athletesRepository.getAllAthletesOnce().toMutableList()
        var athletesCreated = 0
        var athletesUpdated = 0
        var personalBestsImported = 0
        var weightsImported = 0

        for (backupAthlete in backup.athletes) {
            val name = backupAthlete.name.trim()
            if (name.isEmpty() || backupAthlete.dateOfBirthMillis <= 0) continue
            val dateOfBirthMillis = TimeUtils.startOfDayMillis(timeInMillis = backupAthlete.dateOfBirthMillis)

            val matched = AthleteBackupMerge.findMatch(
                athletes = existingAthletes,
                name = name,
                dateOfBirthMillis = dateOfBirthMillis,
            )
            val athleteId = if (matched == null) {
                val newAthlete = AthleteEntity(
                    name = name,
                    dateOfBirth = dateOfBirthMillis,
                    isMale = backupAthlete.isMale,
                    speedCoachSerial = backupAthlete.speedCoachSerial?.trim()?.takeIf { it.isNotEmpty() },
                )
                val newId = athletesRepository.addAthlete(athlete = newAthlete)
                existingAthletes += newAthlete.copy(id = newId)
                athletesCreated++
                newId
            } else {
                AthleteBackupMerge.mergeSerial(
                    existing = matched,
                    fileSerial = backupAthlete.speedCoachSerial,
                )?.let { updated -> athletesRepository.updateAthlete(athlete = updated) }
                athletesUpdated++
                matched.id
            }

            val existingBests = athletesRepository.getPersonalBests(athleteId = athleteId)
            val incomingBests = backupAthlete.personalBests.mapNotNull { best ->
                val boatType = runCatching { TrainingPieceType.valueOf(best.boatType) }.getOrNull()
                    ?: return@mapNotNull null
                if (best.distanceMeters <= 0 || best.timeMillis <= 0) return@mapNotNull null
                AthletePersonalBestEntity(
                    athleteId = athleteId,
                    boatType = boatType,
                    distanceMeters = best.distanceMeters,
                    timeMillis = best.timeMillis,
                )
            }
            val bestsToWrite = AthleteBackupMerge.personalBestsToWrite(
                existing = existingBests,
                incoming = incomingBests,
            )
            athletesRepository.upsertPersonalBests(bests = bestsToWrite)
            personalBestsImported += bestsToWrite.size

            val incomingWeights = backupAthlete.weights.mapNotNull { weight ->
                val dateMillis = TimeUtils.startOfDayMillis(timeInMillis = weight.dateMillis)
                AthleteWeightEntity(
                    athleteId = athleteId,
                    dateMillis = TimeUtils.noonOfDay(timeInMillis = dateMillis),
                    weightTenths = weight.weightTenths,
                ).takeIf {
                    dateMillis <= todayMillis && weight.weightTenths in MIN_WEIGHT_TENTHS..MAX_WEIGHT_TENTHS
                }
            }
            val weightsToWrite = AthleteBackupMerge.weightsToWrite(incoming = incomingWeights)
            athletesRepository.upsertWeights(weights = weightsToWrite)
            weightsImported += weightsToWrite.size
        }

        AthleteTransferResult(
            athletesCreated = athletesCreated,
            athletesUpdated = athletesUpdated,
            personalBestsImported = personalBestsImported,
            weightsImported = weightsImported,
        )
    }

    private suspend fun buildBackupAthlete(athleteId: Long): BackupAthlete? {
        val athlete = athletesRepository.getAthlete(athleteId = athleteId) ?: return null
        return BackupAthlete(
            name = athlete.name,
            dateOfBirthMillis = athlete.dateOfBirth,
            isMale = athlete.isMale,
            speedCoachSerial = athlete.speedCoachSerial,
            personalBests = athletesRepository.getPersonalBests(athleteId = athleteId)
                .sortedWith(compareBy({ it.boatType.name }, { it.distanceMeters }))
                .map { best ->
                    BackupPersonalBest(
                        boatType = best.boatType.name,
                        distanceMeters = best.distanceMeters,
                        timeMillis = best.timeMillis,
                    )
                },
            weights = athletesRepository.getWeights(athleteId = athleteId)
                .sortedBy { it.dateMillis }
                .map { weight ->
                    BackupWeight(
                        dateMillis = weight.dateMillis,
                        weightTenths = weight.weightTenths,
                    )
                },
        )
    }
}