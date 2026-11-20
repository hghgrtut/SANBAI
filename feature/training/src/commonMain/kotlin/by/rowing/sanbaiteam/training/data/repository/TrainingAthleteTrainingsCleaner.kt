package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.domain.trainings.AthleteTrainingsCleaner

internal class TrainingAthleteTrainingsCleaner(private val repository: TrainingRepository) : AthleteTrainingsCleaner {

    override suspend fun countRelatedTrainings(athleteIds: List<Long>): Int =
        repository.countTrainingsForAthletes(athleteIds = athleteIds)

    override suspend fun deleteRelatedTrainings(athleteIds: List<Long>): Int =
        repository.deleteTrainingsForAthletes(athleteIds = athleteIds)
}