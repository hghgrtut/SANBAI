package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailAthlete
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TrainingRepository(
    private val dao: TrainingDao,
    private val athletesRepository: AthletesRepository
) {

    suspend fun saveTraining(
        dateMillis: Long,
        type: TrainingPieceType,
        pieces: List<TrainingAthletePieceEntity>
    ): Long = withContext(Dispatchers.IO) {
        dao.saveTrainingWithPieces(
            training = TrainingEntity(dateMillis = dateMillis, type = type),
            pieces = pieces
        )
    }

    suspend fun getTrainingsList(): List<ListTrainingStateItem> = withContext(Dispatchers.IO) {
        dao.getAllTrainingEntities().mapNotNull { training ->
            val pieces = dao.getPiecesForTraining(training.id)
            if (pieces.isEmpty()) return@mapNotNull null

            val athleteIds = pieces.map { it.athleteId }.distinct()
            val nameMap = athletesRepository.getAthleteNameMap(athleteIds)
            val athleteNames = athleteIds.mapNotNull { nameMap[it] }
            val firstAthletePieces = pieces
                .filter { it.athleteId == athleteIds.first() }
                .sortedBy { it.order }

            ListTrainingStateItem(
                trainingId = training.id,
                trainingTypeResId = training.type.uiResId,
                trainingDate = TimeUtils.formatMillisToString(training.dateMillis),
                trainingWork = formatWorkSummary(firstAthletePieces),
                athletes = athleteNames
            )
        }
    }

    suspend fun getTrainingDetail(trainingId: Long): TrainingDetailState = withContext(Dispatchers.IO) {
        val training = dao.getTrainingById(trainingId)
            ?: return@withContext TrainingDetailState(isLoading = false, notFound = true)

        val pieces = dao.getPiecesForTraining(trainingId)
        val athleteIds = pieces.map { it.athleteId }.distinct()
        val nameMap = athletesRepository.getAthleteNameMap(athleteIds)

        val athletes = athleteIds.map { athleteId ->
            val athletePieces = pieces
                .filter { it.athleteId == athleteId }
                .sortedBy { it.order }
            TrainingDetailAthlete(
                athleteId = athleteId,
                athleteName = nameMap[athleteId].orEmpty(),
                pieces = athletePieces.map { piece ->
                    TrainingDetailPiece(
                        order = piece.order + 1,
                        distanceMeters = piece.distanceMeters,
                        timeFormatted = RowingTimeFormat.formatDuration(piece.timeMillis),
                        paceFormatted = RowingTimeFormat.formatPace(
                            timeMillis = piece.timeMillis,
                            distanceMeters = piece.distanceMeters
                        ).orEmpty(),
                        strokeRateFormatted = RowingTimeFormat.formatStrokeRate(piece.strokeRate)
                    )
                }
            )
        }

        TrainingDetailState(
            trainingId = training.id,
            dateFormatted = TimeUtils.formatMillisToString(training.dateMillis),
            typeResId = training.type.uiResId,
            athletes = athletes,
            isLoading = false,
            notFound = false
        )
    }

    private fun formatWorkSummary(pieces: List<TrainingAthletePieceEntity>): String {
        if (pieces.isEmpty()) return ""
        val groups = LinkedHashMap<Int, Int>()
        for (piece in pieces) {
            groups[piece.distanceMeters] = (groups[piece.distanceMeters] ?: 0) + 1
        }
        return groups.entries.joinToString(separator = " + ") { (distance, count) ->
            if (count > 1) "${count}×${distance}м" else "${distance}м"
        }
    }
}
