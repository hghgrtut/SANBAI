package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.formatTenths
import by.rowing.sanbaiteam.core.util.roundToTenths
import by.rowing.sanbaiteam.settings.data.repository.WorkDisplaySettingsRepository
import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
import by.rowing.sanbaiteam.training.data.comparison.TrainingComparisonCalculator
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartMetric
import by.rowing.sanbaiteam.training.data.models.chart.TrainingChartResult
import by.rowing.sanbaiteam.training.data.models.comparison.ComparablePiece
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailPiece
import by.rowing.sanbaiteam.training.presentation.detail.models.TrainingDetailState
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingPieceItem
import by.rowing.sanbaiteam.training.presentation.list.models.ListTrainingStateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class TrainingRepository(
    private val dao: TrainingDao,
    private val athletesRepository: AthletesRepository,
    private val importStorage: TrainingImportStorage,
    private val chartReader: TrainingChartReader,
    private val workDisplaySettingsRepository: WorkDisplaySettingsRepository,
) {

    suspend fun saveTraining(
        dateMillis: Long,
        type: TrainingPieceType,
        pieces: List<TrainingAthletePieceEntity>,
    ): Long = dao.saveTrainingWithPieces(
        training = TrainingEntity(
            dateMillis = dateMillis,
            type = type,
        ),
        pieces = pieces
    )

    suspend fun updateTrainingImportMetadata(
        trainingId: Long,
        relativePath: String?,
    ) {
        dao.updateImportMetadata(
            trainingId = trainingId,
            relativePath = relativePath,
        )
    }

    suspend fun deleteTrainings(trainingIds: List<Long>) = withContext(Dispatchers.Default) {
        dao.getImportPathsByIds(trainingIds).forEach { relativePath ->
            runCatching { importStorage.deleteImportFile(relativePath) }
        }
        dao.deleteTrainingsWithPieces(trainingIds)
    }

    fun observeTrainingsList(): Flow<List<ListTrainingStateItem>> = combine(
        dao.observeAllTrainingEntities(),
        workDisplaySettingsRepository.modes,
    ) { trainings, modes -> trainings to modes }
        .map { (trainings, modes) ->
            toListItems(
                trainings = trainings,
                modes = modes
            )
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    suspend fun getComparisonCandidates(trainingId: Long): List<ListTrainingStateItem> =
        withContext(Dispatchers.Default) {
            val baseType = dao.getTrainingById(trainingId)?.type ?: return@withContext emptyList()
            toListItems(
                trainings = dao.getAllTrainingEntities().filter { training ->
                    training.type == baseType && training.id != trainingId
                },
                modes = workDisplaySettingsRepository.modes.first(),
            )
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
                    piece.order to ComparablePiece(
                        distanceMeters = piece.distanceMeters,
                        timeMillis = piece.timeMillis,
                        strokeRate = piece.strokeRate,
                        avgHeartRate = piece.avgHeartRate,
                        maxHeartRate = piece.maxHeartRate,
                        recoveryHeartRate = piece.recoveryHeartRate,
                        lactate = piece.lactate,
                        percentOfPb = personalBestPercent(
                            piece = piece,
                            athleteIds = source.athleteIds,
                            personalBests = personalBests,
                        ),
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
                chartMetricsByOrder = loadChartMetrics(
                    training = training,
                    pieces = pieces,
                ),
            ),
            comparedTrainingIds = comparedSources.map { it.trainingId },
            isLoading = false,
            notFound = false
        )
    }

    suspend fun getTrainingChart(
        trainingId: Long,
        order: Int,
    ): TrainingChartResult = withContext(Dispatchers.Default) {
        val training = dao.getTrainingById(trainingId) ?: return@withContext TrainingChartResult.PieceNotFound
        val piece = dao.getPiecesForTraining(trainingId).firstOrNull { it.order == order - 1 }
            ?: return@withContext TrainingChartResult.PieceNotFound
        TrainingChartResult.Success(
            data = chartReader.read(
                sourceFileIndex = piece.sourceFileIndex ?: return@withContext TrainingChartResult.SourceUnavailable,
                sourceFileBytes = getTrainingSourceBytes(training = training)
                    ?: return@withContext TrainingChartResult.SourceUnavailable,
                sourceIntervalIndex = piece.sourceIntervalIndex
                    ?: return@withContext TrainingChartResult.SourceUnavailable,
                pieceType = training.type,
            ) ?: return@withContext TrainingChartResult.SourceUnavailable,
            pieceType = training.type,
        )
    }

    private fun loadChartMetrics(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>,
    ): Map<Int, Set<TrainingChartMetric>> {
        if (pieces.all { it.sourceFileIndex == null || it.sourceIntervalIndex == null }) {
            return emptyMap()
        }
        val sourceBytes = getTrainingSourceBytes(training = training) ?: return emptyMap()
        val cache = mutableMapOf<Pair<Int, Int>, Set<TrainingChartMetric>>()
        return pieces
            .mapNotNull { piece ->
                val fileIndex = piece.sourceFileIndex ?: return@mapNotNull null
                val intervalIndex = piece.sourceIntervalIndex ?: return@mapNotNull null
                piece.order to cache.getOrPut(fileIndex to intervalIndex) {
                    chartReader.read(
                        sourceFileBytes = sourceBytes,
                        sourceFileIndex = fileIndex,
                        sourceIntervalIndex = intervalIndex,
                        pieceType = training.type,
                    )?.availableMetrics.orEmpty()
                }
            }
            .toMap()
    }

    private fun getTrainingSourceBytes(training: TrainingEntity): ByteArray? =
        training.sourceFileRelativePath?.let { importStorage.readImportFile(it) }

    private fun buildDetailPieces(
        baseCrewPieces: List<TrainingAthletePieceEntity>,
        baseAthleteIds: List<Long>,
        personalBests: Map<Long, AthletePersonalBestEntity>,
        comparedByOrder: Map<Int, List<ComparablePiece>>,
        chartMetricsByOrder: Map<Int, Set<TrainingChartMetric>>,
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
            chartMetrics = chartMetricsByOrder[piece.order].orEmpty(),
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

    private suspend fun toListItems(
        trainings: List<TrainingEntity>,
        modes: Map<TrainingPieceType, TrainingWorkDisplayMode>,
    ): List<ListTrainingStateItem> {
        if (trainings.isEmpty()) return emptyList()
        val piecesByTrainingId = dao.getPiecesForTrainings(trainings.map { it.id })
            .groupBy { it.trainingId }
        val nameMap = athletesRepository.getAthleteNameMap(
            athleteIds = piecesByTrainingId.values
                .flatMap { pieces -> pieces.map { it.athleteId } }
                .distinct()
        )
        return trainings.mapNotNull { training ->
            toListItem(
                training = training,
                pieces = piecesByTrainingId[training.id].orEmpty(),
                nameMap = nameMap,
                modes = modes,
            )
        }
    }

    private fun toListItem(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>,
        nameMap: Map<Long, String>,
        modes: Map<TrainingPieceType, TrainingWorkDisplayMode>,
    ): ListTrainingStateItem? {
        if (pieces.isEmpty()) return null

        val athleteIds = pieces.map { it.athleteId }.distinct()
        val athleteNames = athleteIds.mapNotNull { nameMap[it] }

        return ListTrainingStateItem(
            trainingId = training.id,
            trainingType = training.type,
            trainingTypeResId = training.type.uiResId,
            trainingDate = TimeUtils.formatDate(training.dateMillis),
            trainingWork = formatWorkSummary(
                mode = modes.getValue(training.type),
                pieces = pieces
            ),
            athletes = athleteNames,
            dateMillis = training.dateMillis,
            athleteIds = athleteIds,
            pieces = pieces.map { piece ->
                ListTrainingPieceItem(
                    distanceMeters = piece.distanceMeters,
                    timeMillis = piece.timeMillis,
                    lactate = piece.lactate,
                )
            },
        )
    }

    private fun personalBestPercent(
        piece: TrainingAthletePieceEntity,
        athleteIds: List<Long>,
        personalBests: Map<Long, AthletePersonalBestEntity>,
    ): Double? = athleteIds.mapNotNull { athleteId ->
        val personalBest = personalBests[athleteId] ?: return@mapNotNull null
        PersonalBestPercent.calculate(
            pieceDistanceMeters = piece.distanceMeters,
            pieceTimeMillis = piece.timeMillis,
            pbDistanceMeters = personalBest.distanceMeters,
            pbTimeMillis = personalBest.timeMillis,
        )
    }.average().takeIf { !it.isNaN() }?.roundToTenths()

    private fun formatWorkSummary(
        mode: TrainingWorkDisplayMode,
        pieces: List<TrainingAthletePieceEntity>,
    ): String {
        val useTime = mode == TrainingWorkDisplayMode.MINUTES
        val groups = LinkedHashMap<Long, Int>()
        for (piece in pieces) {
            val value = if (useTime) {
                (piece.timeMillis + TimeUtils.MILLIS_PER_SECOND / 2) / TimeUtils.MILLIS_PER_SECOND
            } else {
                piece.distanceMeters.toLong()
            }
            groups[value] = (groups[value] ?: 0) + 1
        }
        return groups.entries.joinToString(separator = " + ") { (value, count) ->
            (if (count > 1) "$count×" else "") + (if (useTime) formatWorkTime(seconds = value) else "${value}м")
        }
    }

    private fun formatWorkTime(seconds: Long): String {
        val minutes = seconds / TimeUtils.SECONDS_PER_MINUTE
        val remainderSeconds = seconds % TimeUtils.SECONDS_PER_MINUTE
        return if (minutes > 0 && remainderSeconds == 0L) {
            "$minutes мин"
        } else {
            RowingTimeFormat.formatDurationMmSs(timeMillis = seconds * TimeUtils.MILLIS_PER_SECOND)
        }
    }

    private data class ComparedTrainingSource(
        val trainingId: Long,
        val athleteIds: List<Long>,
        val crewPieces: List<TrainingAthletePieceEntity>,
    )
}