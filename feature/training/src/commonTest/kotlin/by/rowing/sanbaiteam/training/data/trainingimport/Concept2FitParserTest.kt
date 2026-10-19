package by.rowing.sanbaiteam.training.data.trainingimport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression test for Concept2 RowErg `.fit` files where message definitions contain fields
 * whose sizes differ from the profile fields with the same number in other messages
 * (`file_id` field 2 is 2 bytes, while `session` field 2 is 4 bytes, etc).
 *
 * A decoder that dispatches on the field number alone used to stop consuming bytes for such
 * fields, which desynchronised the whole message stream and made the workout unreadable.
 */
internal class Concept2FitParserTest {

    @Test
    fun decode_workoutWithMixedFieldSizes_keepsMessageAlignment() {
        val workout = decodeFitWorkout(concept2FitSample())

        assertTrue(workout.isRowing)
        assertEquals(listOf(true, false), workout.laps.map { it.isActive })
        assertEquals(2, workout.records.size)
        assertEquals(listOf(140, 150), workout.records.map { it.heartRate })
        assertEquals(listOf(24, 26), workout.records.map { it.cadence })
    }

    @Test
    fun parse_activeLap_becomesSingleInterval() {
        val parsed = Concept2FitParser.parse(concept2FitSample(), fileName = null)

        assertEquals(TrainingImportSource.CONCEPT2_FIT, parsed.source)
        assertEquals(1, parsed.intervals.size)

        val interval = parsed.intervals.single()
        assertEquals(4523, interval.distanceMeters)
        assertEquals(900_000L, interval.timeMillis)
        assertEquals(25.0, interval.strokeRate)
        assertEquals(145, interval.avgHeartRate)
        assertEquals(150, interval.maxHeartRate)
    }

    @Test
    fun parse_sessionStart_isReportedAsWorkoutStart() {
        val parsed = Concept2FitParser.parse(
            concept2FitSample(),
            fileName = null
        )

        assertEquals(fitMillis(START_TS), parsed.startTimeMillis)
    }

    @Test
    fun matches_recognisesFitMagic() {
        assertTrue(Concept2FitParser.matches(concept2FitSample()))
        assertFalse(Concept2FitParser.matches(byteArrayOf(1, 2, 3)))
    }

    private fun fitMillis(fitSeconds: Long): Long =
        (fitSeconds + FIT_EPOCH_OFFSET_SECONDS) * 1_000L

    private fun concept2FitSample(): ByteArray {
        val data = FitWriter().apply {
            // file_id: fields 2, 3 and 5 must be skipped even though session/lap use those numbers.
            definition(
                local = 0,
                global = 0,
                fields = listOf(0 to 1, 1 to 2, 2 to 2, 3 to 4, 4 to 4, 5 to 2)
            )
            dataMessage(
                local = 0,
                u8(4),
                u16(0x0028),
                u16(0),
                u32(0),
                u32(START_TS),
                u16(0),
            )

            // session: sport = fitness_equipment (4), sub_sport = indoor_rowing (14).
            definition(
                local = 1,
                global = 18,
                fields = listOf(253 to 4, 2 to 4, 5 to 1, 6 to 1, 7 to 4, 9 to 4)
            )
            dataMessage(
                local = 1,
                u32(START_TS + 1_260),
                u32(START_TS),
                u8(4),
                u8(14),
                u32(1_260_000),
                u32(485_438),
            )

            // laps: one active (intensity 0) and one rest (intensity 1).
            definition(
                local = 2,
                global = 19,
                fields = listOf(253 to 4, 2 to 4, 7 to 4, 9 to 4, 23 to 1)
            )
            dataMessage(
                local = 2,
                u32(START_TS + 900),
                u32(START_TS),
                u32(900_000),
                u32(452_299),
                u8(0),
            )
            dataMessage(
                local = 2,
                u32(START_TS + 1_260),
                u32(START_TS + 900),
                u32(360_000),
                u32(33_139),
                u8(1),
            )

            // records: fields 5 (distance), 6 (speed) and 7 (power) do not exist in other messages.
            definition(
                local = 3,
                global = 20,
                fields = listOf(253 to 4, 3 to 1, 4 to 1, 5 to 4, 6 to 2, 7 to 2)
            )
            dataMessage(
                local = 3,
                u32(START_TS + 10),
                u8(140),
                u8(24),
                u32(5_000),
                u16(3_600),
                u16(220),
            )
            dataMessage(
                local = 3,
                u32(START_TS + 20),
                u8(150),
                u8(26),
                u32(10_000),
                u16(3_700),
                u16(230),
            )
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

    private class FitWriter {
        private val bytes = ArrayList<Byte>()

        fun definition(local: Int, global: Int, fields: List<Pair<Int, Int>>) {
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

        fun dataMessage(local: Int, vararg fields: ByteArray) {
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

    private companion object {
        const val START_TS = 1_180_000_000L

        fun u8(value: Int): ByteArray = byteArrayOf(value.toByte())

        fun u16(value: Int): ByteArray = u8(value and 0xFF) + u8((value shr 8) and 0xFF)

        fun u32(value: Long): ByteArray = u8((value and 0xFF).toInt()) +
            u8(((value shr 8) and 0xFF).toInt()) +
            u8(((value shr 16) and 0xFF).toInt()) +
            u8(((value shr 24) and 0xFF).toInt())
    }
}
