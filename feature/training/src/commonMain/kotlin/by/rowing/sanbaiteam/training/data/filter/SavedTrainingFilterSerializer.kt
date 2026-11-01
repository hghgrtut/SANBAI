package by.rowing.sanbaiteam.training.data.filter

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.presentation.filter.TrainingFilter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SavedFilterPayload(
    val athleteIds: List<Long> = emptyList(),
    val trainingTypes: List<String> = emptyList(),
    val pieceTimeFrom: Long? = null,
    val pieceTimeTo: Long? = null,
    val pieceDistanceFrom: Int? = null,
    val pieceDistanceTo: Int? = null,
    val lactateOnly: Boolean = false,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
)

/** Кодирует снимок [TrainingFilter] в JSON и обратно для хранения в Room. */
internal object SavedTrainingFilterSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun encode(filter: TrainingFilter): String = json.encodeToString(
        SavedFilterPayload(
            athleteIds = filter.athleteIds.toList(),
            trainingTypes = filter.trainingTypes.map { type -> type.name },
            pieceTimeFrom = filter.pieceTimeRange?.first,
            pieceTimeTo = filter.pieceTimeRange?.last,
            pieceDistanceFrom = filter.pieceDistanceRange?.first,
            pieceDistanceTo = filter.pieceDistanceRange?.last,
            lactateOnly = filter.lactateOnly,
            dateFrom = filter.dateRange?.first,
            dateTo = filter.dateRange?.last,
        )
    )

    fun decode(payload: String): TrainingFilter? = runCatching {
        val data = json.decodeFromString<SavedFilterPayload>(payload)
        val knownTypes = TrainingPieceType.entries.associateBy { type -> type.name }
        TrainingFilter(
            athleteIds = data.athleteIds.toSet(),
            trainingTypes = data.trainingTypes.mapNotNullTo(mutableSetOf<TrainingPieceType>()) { name ->
                knownTypes[name]
            },
            pieceTimeRange = data.pieceTimeFrom?.let { from ->
                data.pieceTimeTo?.let { to -> from..to }
            },
            pieceDistanceRange = data.pieceDistanceFrom?.let { from ->
                data.pieceDistanceTo?.let { to -> from..to }
            },
            lactateOnly = data.lactateOnly,
            dateRange = data.dateFrom?.let { from ->
                data.dateTo?.let { to -> from..to }
            },
        )
    }.getOrNull()
}