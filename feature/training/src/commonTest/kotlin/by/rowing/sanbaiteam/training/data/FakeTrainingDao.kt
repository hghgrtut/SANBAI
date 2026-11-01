package by.rowing.sanbaiteam.training.data

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeTrainingDao(
    trainings: List<TrainingEntity> = emptyList(),
    pieces: List<TrainingAthletePieceEntity> = emptyList(),
) : TrainingDao {

    private val trainingsFlow = MutableStateFlow(trainings.sortedByDescending { it.dateMillis })
    private val piecesFlow = MutableStateFlow(pieces)

    val deletedIds = mutableListOf<List<Long>>()

    val trainings: List<TrainingEntity> get() = trainingsFlow.value

    val pieces: List<TrainingAthletePieceEntity> get() = piecesFlow.value

    fun updateTrainings(trainings: List<TrainingEntity>) {
        trainingsFlow.value = trainings.sortedByDescending { it.dateMillis }
    }

    fun updatePieces(pieces: List<TrainingAthletePieceEntity>) {
        piecesFlow.value = pieces
    }

    override suspend fun insertTraining(training: TrainingEntity): Long {
        val trainingId = training.id.takeIf { it != 0L } ?: nextTrainingId()
        updateTrainings(
            trainings.filterNot { it.id == trainingId } + training.copy(id = trainingId)
        )
        return trainingId
    }

    override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) {
        updatePieces(this.pieces + pieces)
    }

    override suspend fun getAllTrainingEntities(): List<TrainingEntity> = trainings

    override fun observeAllTrainingEntities(): Flow<List<TrainingEntity>> = trainingsFlow.asStateFlow()

    override suspend fun getTrainingEntitiesByIds(trainingIds: List<Long>): List<TrainingEntity> =
        trainings.filter { it.id in trainingIds }

    override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
        trainings.firstOrNull { it.id == trainingId }

    override suspend fun getImportPathsByIds(trainingIds: List<Long>): List<String> =
        trainingIds.mapNotNull { trainingId ->
            trainings.firstOrNull { it.id == trainingId }?.sourceFileRelativePath
        }

    override suspend fun deletePiecesForTrainings(trainingIds: List<Long>) {
        if (trainingIds.isEmpty()) return
        updatePieces(pieces.filterNot { it.trainingId in trainingIds })
    }

    override suspend fun deleteTrainings(trainingIds: List<Long>) {
        if (trainingIds.isEmpty()) return
        deletedIds += trainingIds
        updateTrainings(trainings.filterNot { it.id in trainingIds })
    }

    override suspend fun updateImportMetadata(
        trainingId: Long,
        relativePath: String?,
    ) {
        updateTrainings(
            trainings.map { training ->
                if (training.id == trainingId) {
                    training.copy(sourceFileRelativePath = relativePath)
                } else {
                    training
                }
            }
        )
    }

    override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
        pieces.filter { it.trainingId == trainingId }
            .sortedWith(compareBy({ it.athleteId }, { it.order }))

    override suspend fun getPiecesForTrainings(trainingIds: List<Long>): List<TrainingAthletePieceEntity> =
        pieces.filter { it.trainingId in trainingIds }
            .sortedWith(compareBy({ it.trainingId }, { it.athleteId }, { it.order }))

    override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> =
        pieces.filter { it.trainingId == trainingId }
            .map { it.athleteId }
            .distinct()

    private fun nextTrainingId(): Long = (trainings.maxOfOrNull { it.id } ?: 0L) + 1L
}