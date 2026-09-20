package by.rowing.sanbaiteam.training.data.trainingtransfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.core.util.TimeUtils

internal fun matchBackupAthletesToExisting(
    backupAthletes: List<BackupAthlete>,
    existing: List<AthleteEntity>,
): Map<Int, Long> {
    val result = mutableMapOf<Int, Long>()
    backupAthletes.forEachIndexed { index, backupAthlete ->
        existing.firstOrNull { it.matches(backupAthlete) }?.let { result[index] = it.id }
    }
    return result
}

private fun AthleteEntity.matches(backupAthlete: BackupAthlete): Boolean =
    name.trim().equals(
        other = backupAthlete.name.trim(),
        ignoreCase = true
    ) && TimeUtils.isSameCalendarDay(
        firstMillis = dateOfBirth.time,
        secondMillis = backupAthlete.dateOfBirthMillis
    )