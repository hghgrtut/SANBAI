package by.rowing.sanbaiteam.training.data.trainingimport

import kotlin.math.roundToInt

internal const val FIT_TEST_START_TS = 1_180_000_000L

/** Lap fixture for [concept2FitFile]: distances are meters, times are seconds. */
internal data class FitTestLap(
    val startOffsetSeconds: Long,
    val elapsedSeconds: Long,
    val distanceMeters: Double,
    val intensity: Int = 0,
)

/** Record fixture for [concept2FitFile]: fields left as null are written as FIT invalid values. */
internal data class FitTestRecord(
    val offsetSeconds: Long,
    val heartRate: Int? = null,
    val cadence: Int? = null,
    val distanceMeters: Double? = null,
    val speedMetersPerSecond: Double? = null,
)

/**
 * Builds a minimal Concept2 style `.fit` file: one `file_id`, one `session`, the given laps and
 * records. Used to keep parser tests readable.
 */
internal fun concept2FitFile(
    laps: List<FitTestLap>,
    records: List<FitTestRecord> = emptyList(),
    sport: Int = 4,
    subSport: Int = 14,
    manufacturer: Int = 40,
): ByteArray {
    val totalElapsedSeconds = laps.sumOf { it.elapsedSeconds }
    val totalDistanceRaw = (laps.sumOf { it.distanceMeters } * DISTANCE_SCALE).roundToInt()
    val data = FitTestWriter().apply {
        // file_id: fields 2, 3 and 5 must be skipped even though session/lap use those numbers.
        definition(
            local = 0,
            global = 0,
            fields = listOf(0 to 1, 1 to 2, 2 to 2, 3 to 4, 4 to 4, 5 to 2)
        )
        dataMessage(
            local = 0,
            u8(4),
            u16(manufacturer),
            u16(0),
            u32(0),
            u32(FIT_TEST_START_TS),
            u16(0),
        )

        definition(
            local = 1,
            global = 18,
            fields = listOf(253 to 4, 2 to 4, 5 to 1, 6 to 1, 7 to 4, 9 to 4)
        )
        dataMessage(
            local = 1,
            u32(FIT_TEST_START_TS + totalElapsedSeconds),
            u32(FIT_TEST_START_TS),
            u8(sport),
            u8(subSport),
            u32(totalElapsedSeconds * MILLIS_PER_SECOND),
            u32(totalDistanceRaw.toLong()),
        )

        definition(
            local = 2,
            global = 19,
            fields = listOf(253 to 4, 2 to 4, 7 to 4, 9 to 4, 23 to 1)
        )
        laps.forEach { lap ->
            dataMessage(
                local = 2,
                u32(FIT_TEST_START_TS + lap.startOffsetSeconds + lap.elapsedSeconds),
                u32(FIT_TEST_START_TS + lap.startOffsetSeconds),
                u32(lap.elapsedSeconds * MILLIS_PER_SECOND),
                u32((lap.distanceMeters * DISTANCE_SCALE).roundToInt().toLong()),
                u8(lap.intensity),
            )
        }

        // records: fields 5 (distance), 6 (speed) and 7 (power) do not exist in other messages.
        definition(
            local = 3,
            global = 20,
            fields = listOf(253 to 4, 3 to 1, 4 to 1, 5 to 4, 6 to 2)
        )
        records.forEach { record ->
            dataMessage(
                local = 3,
                u32(FIT_TEST_START_TS + record.offsetSeconds),
                invalidU8(record.heartRate),
                invalidU8(record.cadence),
                invalidU32(record.distanceMeters?.let { (it * DISTANCE_SCALE).roundToInt().toLong() }),
                invalidU16(record.speedMetersPerSecond?.let { (it * SPEED_SCALE).roundToInt() }),
            )
        }
    }.toByteArray()

    return buildFitFile(data)
}

private fun buildFitFile(data: ByteArray): ByteArray {
    val header = ByteArray(FIT_HEADER_MIN_SIZE)
    header[0] = FIT_HEADER_MIN_SIZE.toByte()
    header[1] = 0x10
    header[4] = (data.size and 0xFF).toByte()
    header[5] = ((data.size shr 8) and 0xFF).toByte()
    header[6] = ((data.size shr 16) and 0xFF).toByte()
    header[7] = ((data.size shr 24) and 0xFF).toByte()
    header[FIT_HEADER_MAGIC_OFFSET] = '.'.code.toByte()
    header[FIT_HEADER_MAGIC_OFFSET + 1] = 'F'.code.toByte()
    header[FIT_HEADER_MAGIC_OFFSET + 2] = 'I'.code.toByte()
    header[FIT_HEADER_MAGIC_OFFSET + 3] = 'T'.code.toByte()
    return header + data + byteArrayOf(0, 0)
}

private const val DISTANCE_SCALE = 100.0
private const val SPEED_SCALE = 1000.0
private const val MILLIS_PER_SECOND = 1_000L

private fun u8(value: Int): ByteArray = byteArrayOf(value.toByte())

private fun u16(value: Int): ByteArray = u8(value and 0xFF) + u8((value shr 8) and 0xFF)

private fun u32(value: Long): ByteArray = u8((value and 0xFF).toInt()) +
        u8(((value shr 8) and 0xFF).toInt()) +
        u8(((value shr 16) and 0xFF).toInt()) +
        u8(((value shr 24) and 0xFF).toInt())

private fun invalidU8(value: Int?): ByteArray = u8(value ?: 0xFF)

private fun invalidU16(value: Int?): ByteArray = if (value == null) u16(0xFFFF) else u16(value)

private fun invalidU32(value: Long?): ByteArray = if (value == null) u32(0xFFFFFFFFL) else u32(value)

private class FitTestWriter {
    private val bytes = ArrayList<Byte>()

    fun definition(
        local: Int,
        global: Int,
        fields: List<Pair<Int, Int>>
    ) {
        byte(DEFINITION_MASK or local)
        byte(0)
        byte(0)
        ushort(global)
        byte(fields.size)
        fields.forEach { (num, size) ->
            byte(num)
            byte(size)
            byte(0)
        }
    }

    fun dataMessage(
        local: Int,
        vararg fields: ByteArray
    ) {
        byte(local)
        fields.forEach { field -> field.forEach { byte(it.toInt() and 0xFF) } }
    }

    fun toByteArray(): ByteArray = bytes.toByteArray()

    private fun byte(value: Int) {
        bytes.add(value.toByte())
    }

    private fun ushort(value: Int) {
        byte(value and 0xFF)
        byte((value shr 8) and 0xFF)
    }
}