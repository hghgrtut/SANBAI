package by.rowing.sanbaiteam.athlete.domain.transfer

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal object AthleteBackupMerge {

    private val WHITESPACE_REGEX = Regex("\\s+")

    fun findMatch(
        athletes: List<AthleteEntity>,
        name: String,
        dateOfBirthMillis: Long,
    ): AthleteEntity? {
        val normalizedName = normalizeName(name = name)
        return athletes.firstOrNull { athlete ->
            normalizeName(name = athlete.name).equals(
                other = normalizedName,
                ignoreCase = true,
            ) && TimeUtils.isSameCalendarDay(
                firstMillis = athlete.dateOfBirth,
                secondMillis = dateOfBirthMillis,
            )
        }
    }

    /** Возвращает обновлённого спортсмена или null, если serial менять не нужно. */
    fun mergeSerial(
        existing: AthleteEntity,
        fileSerial: String?,
    ): AthleteEntity? = fileSerial?.trim()
        ?.takeIf { it.isNotEmpty() && it != existing.speedCoachSerial }
        ?.let { serial ->existing.copy(speedCoachSerial = serial) }

    /** Возвращает рекорды из файла с id существующих записей, чтобы конфликт обновился, а не задвоился. */
    fun personalBestsToWrite(
        existing: List<AthletePersonalBestEntity>,
        incoming: List<AthletePersonalBestEntity>,
    ): List<AthletePersonalBestEntity> {
        val existingIds = existing.associate { best -> best.key() to best.id }
        return incoming
            .associateBy { best -> best.key() }
            .map { (key, best) ->
                best.copy(id = existingIds[key] ?: 0)
            }
    }

    /** Оставляет по одной записи веса на дату; последняя запись в файле побеждает. */
    fun weightsToWrite(incoming: List<AthleteWeightEntity>): List<AthleteWeightEntity> =
        incoming.associateBy { weight -> weight.dateMillis }.values.toList()

    private fun normalizeName(name: String): String =
        name.trim().split(WHITESPACE_REGEX).joinToString(separator = " ")

    private fun AthletePersonalBestEntity.key(): Pair<TrainingPieceType, Int> = boatType to distanceMeters
}