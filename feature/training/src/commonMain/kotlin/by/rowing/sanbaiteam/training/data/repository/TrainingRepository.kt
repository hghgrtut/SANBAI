package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.training.data.comparison.TrainingComparisonCalculator
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.models.comparison.ComparablePiece
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class TrainingRepository(
    private val dao: TrainingDao,
    private val athletesRepository: AthletesRepository,
    private val importStorage: TrainingImportStorage,
) {

    suspend fun saveTraining(
        dateMillis: Long,
        type: TrainingPieceType,
        pieces: List<TrainingAthletePieceEntity>,
        sourceDeviceSerial: String? = null,
        sourceSessionName: String? = null,
    ): Long = dao.saveTrainingWithPieces(
        training = TrainingEntity(
            dateMillis = dateMillis,
            type = type,
            sourceDeviceSerial = sourceDeviceSerial,
            sourceSessionName = sourceSessionName
        ),
        pieces = pieces
    )

    suspend fun updateTrainingImportMetadata(
        trainingId: Long,
        relativePath: String?,
        deviceSerial: String?,
        sessionName: String?
    ) {
        dao.updateImportMetadata(
            trainingId = trainingId,
            relativePath = relativePath,
            deviceSerial = deviceSerial,
            sessionName = sessionName
        )
    }

    suspend fun deleteTrainings(trainingIds: List<Long>) = withContext(Dispatchers.Default) {
        dao.getCsvPathsByIds(trainingIds).forEach { relativePath ->
            runCatching { importStorage.deleteCsv(relativePath) }
        }
        dao.deleteTrainingsWithPieces(trainingIds)
    }

    suspend fun getTrainingsList(): List<ListTrainingStateItem> = withContext(Dispatchers.Default) {
        dao.getAllTrainingEntities().mapNotNull { training -> toListItem(training) }
    }

    suspend fun getComparisonCandidates(trainingId: Long): List<ListTrainingStateItem> =
        withContext(Dispatchers.Default) {
            val baseType = dao.getTrainingById(trainingId)?.type ?: return@withContext emptyList()
            dao.getAllTrainingEntities().mapNotNull { training ->
                if (training.type != baseType || training.id == trainingId) null else toListItem(training)
            }
        }

    suspend fun getTrainingDetail(
        trainingId: Long,
        comparedTrainingIds: List<Long> = emptyList(),
    ): TrainingDetailState = withContext(Dispatchers.Default) {
        val training = dao.getTrainingById(trainingId) ?: return@withContext TrainingDetailState(
            isLoading = false,
            notFound = true
        )

        val pieces = dao.getPiecesForTraining(trainingId)
        val baseAthleteIds = pieces.map { it.athleteId }.distinct()
        val comparedSources = loadComparedSources(
            base = training,
            comparedTrainingIds = comparedTrainingIds,
        )

        val allAthleteIds = (baseAthleteIds + comparedSources.flatMap { it.athleteIds }).distinct()
        val personalBests = athletesRepository.getPersonalBestsForAthletes(
            athleteIds = allAthleteIds,
            boatType = training.type,
            distanceMeters = PersonalBestPercent.DEFAULT_PB_DISTANCE_METERS,
        )
        val nameMap = athletesRepository.getAthleteNameMap(baseAthleteIds)
        val comparedByOrder = comparedSources
            .flatMap { source ->
                source.crewPieces.map { piece ->
                    piece.order to source.toComparablePiece(
                        piece = piece,
                        personalBests = personalBests,
                    )
                }
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second },
            )

        TrainingDetailState(
            trainingId = training.id,
            dateFormatted = TimeUtils.formatDate(training.dateMillis),
            typeResId = training.type.uiResId,
            athleteNames = baseAthleteIds.mapNotNull { nameMap[it] },
            pieces = buildDetailPieces(
                baseCrewPieces = pieces,
                baseAthleteIds = baseAthleteIds,
                personalBests = personalBests,
                comparedByOrder = comparedByOrder,
            ),
            comparedTrainingIds = comparedSources.map { it.trainingId },
            isLoading = false,
            notFound = false
        )
    }

    private fun buildDetailPieces(
        baseCrewPieces: List<TrainingAthletePieceEntity>,
        baseAthleteIds: List<Long>,
        personalBests: Map<Long, AthletePersonalBestEntity>,
        comparedByOrder: Map<Int, List<ComparablePiece>>,
    ): List<TrainingDetailPiece> = baseCrewPieces.map { piece ->
        val percentOfPb = personalBestPercent(
            piece = piece,
            athleteIds = baseAthleteIds,
            personalBests = personalBests,
        )
        TrainingDetailPiece(
            order = piece.order + 1,
            distanceMeters = piece.distanceMeters,
            timeFormatted = RowingTimeFormat.formatDuration(piece.timeMillis),
            paceFormatted = RowingTimeFormat.formatPace(
                timeMillis = piece.timeMillis,
                distanceMeters = piece.distanceMeters
            ).orEmpty(),
            strokeRateFormatted = formatTenths(value = piece.strokeRate),
            avgHeartRate = piece.avgHeartRate,
            maxHeartRate = piece.maxHeartRate,
            recoveryHeartRate = piece.recoveryHeartRate,
            lactate = piece.lactate,
            percentOfPbFormatted = PersonalBestPercent.format(percentOfPb),
            comparison = TrainingComparisonCalculator.compare(
                base = ComparablePiece(
                    distanceMeters = piece.distanceMeters,
                    timeMillis = piece.timeMillis,
                    strokeRate = piece.strokeRate,
                    avgHeartRate = piece.avgHeartRate,
                    maxHeartRate = piece.maxHeartRate,
                    recoveryHeartRate = piece.recoveryHeartRate,
                    lactate = piece.lactate,
                    percentOfPb = percentOfPb,
                ),
                compared = comparedByOrder[piece.order].orEmpty(),
            ),
        )
    }

    private suspend fun loadComparedSources(
        base: TrainingEntity,
        comparedTrainingIds: List<Long>,
    ): List<ComparedTrainingSource> = comparedTrainingIds.distinct().mapNotNull { trainingId ->
        if (trainingId == base.id) return@mapNotNull null
        dao.getTrainingById(trainingId)?.takeIf { it.type == base.type } ?: return@mapNotNull null
        val pieces = dao.getPiecesForTraining(trainingId).ifEmpty { return@mapNotNull null }
        ComparedTrainingSource(
            trainingId = trainingId,
            athleteIds = pieces.map { it.athleteId }.distinct(),
            crewPieces = pieces,
        )
    }

    private suspend fun toListItem(training: TrainingEntity): ListTrainingStateItem? {
        val pieces = dao.getPiecesForTraining(training.id)
        if (pieces.isEmpty()) return null

        val athleteIds = pieces.map { it.athleteId }.distinct()
        val nameMap = athletesRepository.getAthleteNameMap(athleteIds)
        val athleteNames = athleteIds.mapNotNull { nameMap[it] }

        return ListTrainingStateItem(
            trainingId = training.id,
            trainingType = training.type,
            trainingTypeResId = training.type.uiResId,
            trainingDate = TimeUtils.formatDate(training.dateMillis),
            trainingWork = formatWorkSummary(pieces),
            athletes = athleteNames
        )
    }

    private fun personalBestPercent(
        piece: TrainingAthletePieceEntity,
        athleteIds: List<Long>,
        personalBests: Map<Long, AthletePersonalBestEntity>,
    ): Double? {
        val crewPercents = athleteIds.mapNotNull { athleteId ->
            val personalBest = personalBests[athleteId] ?: return@mapNotNull null
            PersonalBestPercent.calculate(
                pieceDistanceMeters = piece.distanceMeters,
                pieceTimeMillis = piece.timeMillis,
                pbDistanceMeters = personalBest.distanceMeters,
                pbTimeMillis = personalBest.timeMillis,
            )
        }
        return PersonalBestPercent.average(crewPercents)
    }

    private fun ComparedTrainingSource.toComparablePiece(
        piece: TrainingAthletePieceEntity,
        personalBests: Map<Long, AthletePersonalBestEntity>,
    ): ComparablePiece = ComparablePiece(
        distanceMeters = piece.distanceMeters,
        timeMillis = piece.timeMillis,
        strokeRate = piece.strokeRate,
        avgHeartRate = piece.avgHeartRate,
        maxHeartRate = piece.maxHeartRate,
        recoveryHeartRate = piece.recoveryHeartRate,
        lactate = piece.lactate,
        percentOfPb = personalBestPercent(
            piece = piece,
            athleteIds = athleteIds,
            personalBests = personalBests,
        ),
    )

    private fun formatWorkSummary(pieces: List<TrainingAthletePieceEntity>): String {
        if (pieces.isEmpty()) return ""
        val groups = LinkedHashMap<Int, Int>()
        for (piece in pieces) {
            groups[piece.distanceMeters] = (groups[piece.distanceMeters] ?: 0) + 1
        }
        return groups.entries.joinToString(separator = " + ") { (distance, count) ->
            (if (count > 1) "$count×" else "") + "${distance}м"
        }
    }

    private data class ComparedTrainingSource(
        val trainingId: Long,
        val athleteIds: List<Long>,
        val crewPieces: List<TrainingAthletePieceEntity>,
    )
}