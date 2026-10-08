package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.util.TimeUtils
import com.garmin.fit.DateTime
import com.garmin.fit.FileEncoder
import com.garmin.fit.FileIdMesg
import com.garmin.fit.Fit
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.Mesg
import com.garmin.fit.RecordMesg
import com.garmin.fit.SessionMesg
import com.garmin.fit.Sport
import com.garmin.fit.SubSport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Date
import kotlin.math.roundToInt
import com.garmin.fit.File as GarminFile

@Suppress("MaxLineLength")
internal class Concept2FitParserTest {

    @Test
    fun parse_pm5IntervalWorkout_returnsOnlyActiveLapsWithRecoveryHeartRate() {
        val fixture = buildFixture()

        val parsed = Concept2FitParser.parse(fixture.bytes)

        assertEquals(TrainingImportSource.CONCEPT2_FIT, parsed.source)
        assertNull(parsed.deviceSerial)
        assertNull(parsed.sessionName)
        assertEquals(START_MILLIS, parsed.startTimeMillis)
        assertEquals(2, parsed.intervals.size)

        val firstPiece = parsed.intervals[0]
        assertEquals(4464, firstPiece.distanceMeters)
        assertEquals(WORK_MILLIS, firstPiece.timeMillis)
        assertEquals(FIRST_ACTIVE_CADENCE.toDouble(), firstPiece.strokeRate, 0.001)
        assertEquals(fixture.firstActiveHeartRates.average().roundToInt(), firstPiece.avgHeartRate)
        assertEquals(fixture.firstActiveHeartRates.max(), firstPiece.maxHeartRate)
        assertEquals(REST_SAMPLE_HEART_RATE, firstPiece.recoveryHeartRate)
    }

    @Test
    fun parse_activeLapWithoutFollowingRest_hasNoRecoveryHeartRate() {
        val fixture = buildFixture()

        val lastPiece = Concept2FitParser.parse(fixture.bytes).intervals.last()

        assertEquals(4460, lastPiece.distanceMeters)
        assertEquals(LAST_ACTIVE_CADENCE.toDouble(), lastPiece.strokeRate, 0.001)
        assertNull(lastPiece.recoveryHeartRate)
    }

    @Test
    fun parse_recordOnLastLapEndTimestamp_isIncludedInLastLap() {
        val fixture = buildFixture()

        val lastPiece = Concept2FitParser.parse(fixture.bytes).intervals.last()

        assertEquals(LAST_LAP_END_HEART_RATE, lastPiece.maxHeartRate)
    }

    @Test
    fun parse_restLapsAreStoredAsArchiveRows_withoutBecomingPieces() {
        val fixture = buildFixture()

        val parsed = Concept2FitParser.parse(fixture.bytes)
        val laps = Concept2LapCsv.decode(parsed.csvText)

        assertTrue(Concept2LapCsv.isArchive(parsed.csvText))
        assertEquals(3, laps.size)
        assertEquals(
            listOf(Concept2LapIntensity.ACTIVE, Concept2LapIntensity.REST, Concept2LapIntensity.ACTIVE),
            laps.map { it.intensity }
        )
        assertEquals(listOf(900_000L, 360_000L, 900_000L), laps.map { it.totalElapsedTimeMillis })
    }

    @Test
    fun parse_nonRowingFitFile_isRejected() {
        val bytes = encode(
            listOf(
                fileId(START_MILLIS),
                session(START_MILLIS).apply {
                    this.sport = Sport.RUNNING
                    this.subSport = SubSport.GENERIC
                },
                lap(Concept2LapIntensity.ACTIVE, START_MILLIS, WORK_MILLIS, 4464f),
                record(START_MILLIS, 160, 24),
            )
        )

        assertThrows(IllegalArgumentException::class.java) { Concept2FitParser.parse(bytes) }
    }

    @Test
    fun parse_fileWithoutFitHeader_isRejected() {
        val bytes = "Interval Summaries:\n1,500.0".toByteArray()

        assertThrows(IllegalArgumentException::class.java) { Concept2FitParser.parse(bytes) }
    }

    @Test
    fun matches_fitFileAndSpeedCoachCsv_areDetectedBySource() {
        assertTrue(Concept2FitParser.matches(buildFixture().bytes))
        assertTrue(SpeedCoachCsvParser.matches(speedCoachCsv().toByteArray()))

        assertFalse(Concept2FitParser.matches(speedCoachCsv().toByteArray()))
        assertFalse(SpeedCoachCsvParser.matches(buildFixture().bytes))
    }

    @Test
    fun parseAndMerge_twoFiles_concatenateLapArchives() {
        val first = Concept2FitParser.parse(buildFixture().bytes)
        val second = Concept2FitParser.parse(buildFixture().bytes)

        val merged = Concept2FitParser.merge(listOf(first.csvText, second.csvText))

        assertEquals(6, Concept2LapCsv.decode(merged).size)
    }

    private fun buildFixture(): Fixture {
        val activeStart = START_MILLIS
        val restStart = activeStart + WORK_MILLIS
        val lastActiveStart = restStart + REST_MILLIS

        val firstHeartRates = (0 until WORK_RECORD_COUNT).map { index -> 150 + index % 20 }
        val lastHeartRates = (0 until WORK_RECORD_COUNT).map { index -> 180 + index % 5 }

        val messages = mutableListOf<Mesg>()
        messages += fileId(activeStart)
        messages += session(activeStart)
        messages += lap(Concept2LapIntensity.ACTIVE, activeStart, WORK_MILLIS, 4464f)
        messages += lap(Concept2LapIntensity.REST, restStart, REST_MILLIS, 420f)
        messages += lap(Concept2LapIntensity.ACTIVE, lastActiveStart, WORK_MILLIS, 4460f)
        messages += recordsInLap(activeStart, firstHeartRates, FIRST_ACTIVE_CADENCE)
        messages += restRecords(restStart)
        messages += recordsInLap(lastActiveStart, lastHeartRates, LAST_ACTIVE_CADENCE)
        messages += record(lastActiveStart + WORK_MILLIS, LAST_LAP_END_HEART_RATE, LAST_ACTIVE_CADENCE)

        return Fixture(
            bytes = encode(messages),
            firstActiveHeartRates = firstHeartRates,
        )
    }

    private fun fileId(startMillis: Long): FileIdMesg = FileIdMesg().apply {
        type = GarminFile.ACTIVITY
        manufacturer = CONCEPT2_MANUFACTURER
        timeCreated = fitDateTime(startMillis)
    }

    private fun session(startMillis: Long): SessionMesg = SessionMesg().apply {
        sport = Sport.FITNESS_EQUIPMENT
        subSport = SubSport.INDOOR_ROWING
        timestamp = fitDateTime(startMillis)
        startTime = fitDateTime(startMillis)
        firstLapIndex = 0
        numLaps = LAP_COUNT
        totalElapsedTime = (WORK_MILLIS * 2 + REST_MILLIS) / TimeUtils.MILLIS_PER_SECOND.toFloat()
        totalDistance = 9344f
    }

    private fun lap(
        intensity: Concept2LapIntensity,
        startMillis: Long,
        elapsedMillis: Long,
        distanceMeters: Float
    ): LapMesg = LapMesg().apply {
        this.intensity = if (intensity == Concept2LapIntensity.ACTIVE) Intensity.ACTIVE else Intensity.REST
        startTime = fitDateTime(startMillis)
        timestamp = fitDateTime(startMillis + elapsedMillis)
        totalElapsedTime = (elapsedMillis / TimeUtils.MILLIS_PER_SECOND).toFloat()
        totalDistance = distanceMeters
    }

    private fun recordsInLap(
        startMillis: Long,
        heartRates: List<Int>,
        cadence: Int
    ): List<RecordMesg> {
        val elapsedMillis = WORK_MILLIS
        return heartRates.mapIndexedNotNull { index, heartRate ->
            val offset = index * RECORD_INTERVAL_MILLIS
            if (offset >= elapsedMillis) return@mapIndexedNotNull null
            record(startMillis + offset, heartRate, cadence)
        }
    }

    private fun restRecords(restStartMillis: Long): List<RecordMesg> = listOf(
        record(restStartMillis, 140, 20),
        record(restStartMillis + REST_BEFORE_OFFSET_MILLIS, 130, 20),
        record(restStartMillis + REST_AFTER_OFFSET_MILLIS, 120, 20),
    )

    private fun record(
        timestampMillis: Long,
        heartRate: Int,
        cadence: Int
    ): RecordMesg = RecordMesg().apply {
        timestamp = fitDateTime(timestampMillis)
        this.heartRate = heartRate.toShort()
        this.cadence = cadence.toShort()
    }

    private fun encode(messages: List<Mesg>): ByteArray {
        val file = File.createTempFile("concept2_fit_test", FIT_EXTENSION)
        file.deleteOnExit()
        val encoder = FileEncoder(file, Fit.ProtocolVersion.V1_0)
        messages.forEach(encoder::write)
        encoder.close()
        return file.readBytes()
    }

    private fun fitDateTime(millis: Long): DateTime = DateTime(Date(millis))

    private fun speedCoachCsv(): String = "Session Information:,,,Device Information:\r\nInterval Summaries:\r\n"

    private companion object {

        const val START_MILLIS = 1_789_000_000_000L
        const val WORK_MILLIS = 900_000L
        const val REST_MILLIS = 360_000L
        const val RECORD_INTERVAL_MILLIS = 10_000L
        const val WORK_RECORD_COUNT = 90
        const val LAP_COUNT = 3
        const val FIRST_ACTIVE_CADENCE = 24
        const val LAST_ACTIVE_CADENCE = 22
        const val LAST_LAP_END_HEART_RATE = 200
        const val REST_BEFORE_OFFSET_MILLIS = 80_000L
        const val REST_AFTER_OFFSET_MILLIS = 100_000L
        const val REST_SAMPLE_HEART_RATE = 125
        const val CONCEPT2_MANUFACTURER = 40
        const val FIT_EXTENSION = ".fit"
    }

    private data class Fixture(
        val bytes: ByteArray,
        val firstActiveHeartRates: List<Int>,
    )
}