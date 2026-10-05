package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.trainingimport.CsvWriter.cell

internal enum class Concept2LapIntensity {
    ACTIVE,
    REST,
}

internal data class Concept2LapSummary(
    val intensity: Concept2LapIntensity,
    val startTimeMillis: Long,
    val totalElapsedTimeMillis: Long,
    val totalDistanceMeters: Double,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val avgCadence: Int?,
)

/**
 * Compact CSV archive of Concept2 laps. It replaces the original .fit file after import,
 * so backup format and stored trainings do not depend on binary workout files.
 */
internal object Concept2LapCsv {

    const val MARKER = "# SANBAITeam Concept2 lap archive v1"

    private const val COMMENT = MARKER
    private const val HEADER = "lap,intensity,startTime,elapsed,distance,avgHr,maxHr,avgCadence"

    private const val COLUMN_LAP = 0
    private const val COLUMN_INTENSITY = 1
    private const val COLUMN_START_TIME = 2
    private const val COLUMN_ELAPSED = 3
    private const val COLUMN_DISTANCE = 4
    private const val COLUMN_AVG_HEART_RATE = 5
    private const val COLUMN_MAX_HEART_RATE = 6
    private const val COLUMN_AVG_CADENCE = 7

    fun encode(laps: List<Concept2LapSummary>): String = buildString {
        appendLine(COMMENT)
        appendLine(HEADER)
        laps.forEachIndexed { index, lap -> appendLine(rowOf(index + 1, lap)) }
    }.trimEnd()

    fun isArchive(csvText: String): Boolean = csvText.trimStart().startsWith(MARKER)

    fun decode(csvText: String): List<Concept2LapSummary> = csvText.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull(::parseRow)
        .toList()

    fun merge(csvTexts: List<String>): String {
        require(csvTexts.isNotEmpty()) { "At least one CSV is required." }
        val laps = csvTexts.flatMap(::decode)
        if (laps.isEmpty()) return csvTexts.first()
        return encode(laps)
    }

    private fun rowOf(
        lapNumber: Int,
        lap: Concept2LapSummary
    ): String = CsvWriter.row(
        lapNumber.toString(),
        lap.intensity.name,
        lap.startTimeMillis.cell(),
        lap.totalElapsedTimeMillis.cell(),
        lap.totalDistanceMeters.cell(),
        lap.avgHeartRate.cell(),
        lap.maxHeartRate.cell(),
        lap.avgCadence.cell(),
    )

    private fun parseRow(line: String): Concept2LapSummary? {
        val columns = line.split(',')
        if (columns.getOrNull(COLUMN_LAP)?.trim()?.toIntOrNull() == null) return null
        val intensity = columns.getOrNull(COLUMN_INTENSITY)
            ?.trim()
            ?.let { name -> Concept2LapIntensity.entries.firstOrNull { it.name == name } }
            ?: return null
        return Concept2LapSummary(
            intensity = intensity,
            startTimeMillis = columns.longAt(COLUMN_START_TIME) ?: 0L,
            totalElapsedTimeMillis = columns.longAt(COLUMN_ELAPSED) ?: 0L,
            totalDistanceMeters = columns.doubleAt(COLUMN_DISTANCE) ?: 0.0,
            avgHeartRate = columns.intAt(COLUMN_AVG_HEART_RATE),
            maxHeartRate = columns.intAt(COLUMN_MAX_HEART_RATE),
            avgCadence = columns.intAt(COLUMN_AVG_CADENCE),
        )
    }

    private fun List<String>.intAt(index: Int): Int? = getOrNull(index)?.trim()?.toDoubleOrNull()?.toInt()

    private fun List<String>.longAt(index: Int): Long? = getOrNull(index)?.trim()?.toDoubleOrNull()?.toLong()

    private fun List<String>.doubleAt(index: Int): Double? = getOrNull(index)?.trim()?.toDoubleOrNull()
}