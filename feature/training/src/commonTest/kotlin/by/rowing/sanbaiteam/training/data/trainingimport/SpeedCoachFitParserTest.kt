package by.rowing.sanbaiteam.training.data.trainingimport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SpeedCoachFitParserTest {

    @Test
    fun matches_speedCoachSport_returnsTrue_andConcept2ReturnsFalse() {
        val speedCoach = speedCoachFit()

        assertTrue(SpeedCoachFitParser.matches(speedCoach))
        assertFalse(Concept2FitParser.matches(speedCoach))
    }

    @Test
    fun parse_lapsWithoutIntensity_becomeActiveIntervals() {
        val parsed = SpeedCoachFitParser.parse(speedCoachFit(), fileName = null)

        assertEquals(TrainingImportSource.SPEED_COACH, parsed.source)
        assertEquals(FIT_FILE_EXTENSION, parsed.fileExtension)
        assertEquals("2693559", parsed.deviceSerial)
        assertEquals((START_TS + FIT_EPOCH_OFFSET_SECONDS) * 1_000L, parsed.startTimeMillis)
        assertEquals(2, parsed.intervals.size)

        val first = parsed.intervals[0]
        assertEquals(1001, first.distanceMeters)
        assertEquals(202_900L, first.timeMillis)
        assertEquals(32.0, first.strokeRate)
        assertEquals(150, first.avgHeartRate)
        assertEquals(160, first.maxHeartRate)
        assertNull(first.recoveryHeartRate)

        val second = parsed.intervals[1]
        assertEquals(1000, second.distanceMeters)
        assertEquals(201_900L, second.timeMillis)
        assertEquals(38.0, second.strokeRate)
        assertEquals(140, second.avgHeartRate)
        assertEquals(145, second.maxHeartRate)
    }

    @Test
    fun parse_withoutHeartRateRecords_hasNullHeartRates() {
        val parsed = SpeedCoachFitParser.parse(speedCoachFit(withHeartRate = false), fileName = null)

        assertEquals(2, parsed.intervals.size)
        assertTrue(parsed.intervals.all { it.avgHeartRate == null && it.maxHeartRate == null })
    }

    @Test
    fun parse_keepsOriginalFitBytes() {
        val bytes = speedCoachFit()

        assertTrue(bytes.contentEquals(SpeedCoachFitParser.parse(bytes, null).rawFile))
    }

    private fun speedCoachFit(withHeartRate: Boolean = true): ByteArray {
        val data = FitWriter().apply {
            definition(
                local = 0,
                global = 0,
                fields = listOf(0 to 1, 1 to 2, 2 to 2, 3 to 4, 4 to 4)
            )
            dataMessage(
                local = 0,
                u8(4),
                u16(87),
                u16(0),
                u32(2_693_559),
                u32(START_TS),
            )

            definition(
                local = 1,
                global = 18,
                fields = listOf(253 to 4, 2 to 4, 5 to 1, 6 to 1, 7 to 4, 9 to 4)
            )
            dataMessage(
                local = 1,
                u32(START_TS + 405),
                u32(START_TS),
                u8(15),
                u8(0),
                u32(404_800),
                u32(200_093),
            )

            // SpeedCoach laps carry no `intensity` field at all.
            definition(
                local = 2,
                global = 19,
                fields = listOf(253 to 4, 2 to 4, 7 to 4, 9 to 4)
            )
            dataMessage(
                local = 2,
                u32(START_TS + 202),
                u32(START_TS),
                u32(202_900),
                u32(100_063),
            )
            dataMessage(
                local = 2,
                u32(START_TS + 405),
                u32(START_TS + 202),
                u32(201_900),
                u32(100_030),
            )

            definition(
                local = 3,
                global = 20,
                fields = listOf(253 to 4, 3 to 1, 4 to 1)
            )
            listOf(150, 160, 140).forEachIndexed { index, heartRate ->
                dataMessage(
                    local = 3,
                    u32(START_TS + 10L * (index + 1)),
                    if (withHeartRate) u8(heartRate) else u8(0xFF),
                    u8(32),
                )
            }
            listOf(140, 145, 135).forEachIndexed { index, heartRate ->
                dataMessage(
                    local = 3,
                    u32(START_TS + 300L + 10L * index),
                    if (withHeartRate) u8(heartRate) else u8(0xFF),
                    u8(38),
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

    private companion object {
        const val START_TS = 1_155_366_499L

        fun u8(value: Int): ByteArray = byteArrayOf(value.toByte())

        fun u16(value: Int): ByteArray = u8(value and 0xFF) + u8((value shr 8) and 0xFF)

        fun u32(value: Long): ByteArray = u8((value and 0xFF).toInt()) +
                u8(((value shr 8) and 0xFF).toInt()) +
                u8(((value shr 16) and 0xFF).toInt()) +
                u8(((value shr 24) and 0xFF).toInt())
    }

    private class FitWriter {
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
}
