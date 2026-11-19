package by.rowing.sanbaiteam.athlete.domain.trainings

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Связь удаления спортсменов с тренировками.
 * Реализуется в feature:training, чтобы не тянуть тренировки в feature:athlete.
 */
@AllowDetektPublic
interface AthleteTrainingsCleaner {

    /** Сколько разных тренировок связано с указанными спортсменами. */
    suspend fun countRelatedTrainings(athleteIds: List<Long>): Int

    /** Удаляет тренировки, в которых участвуют указанные спортсмены. Возвращает их количество. */
    suspend fun deleteRelatedTrainings(athleteIds: List<Long>): Int
}