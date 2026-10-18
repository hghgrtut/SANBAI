package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.util.TimeUtils
import com.garmin.fit.DateTime
import com.garmin.fit.FitDecoder
import com.garmin.fit.Intensity
import com.garmin.fit.LapMesg
import com.garmin.fit.RecordMesg
import com.garmin.fit.SessionMesg
import com.garmin.fit.Sport
import com.garmin.fit.SubSport
import java.io.ByteArrayInputStream

internal actual fun decodeFitWorkout(bytes: ByteArray): FitWorkout {
    val fit = FitDecoder().decode(ByteArrayInputStream(bytes))
    val session = fit.sessionMesgs.firstOrNull()
    return FitWorkout(
        isRowing = session.isRowingWorkout(),
        startTimeMillis = (
                session?.startTime
                    ?: session?.timestamp
                    ?: fit.fileIdMesgs.firstOrNull()?.timeCreated
                )?.toEpochMillis(),
        laps = fit.lapMesgs.mapNotNull(::readLap),
        records = fit.recordMesgs.mapNotNull(::readRecord),
    )
}

private fun readLap(lap: LapMesg): FitLap? {
    val isActive = when (lap.intensity) {
        Intensity.ACTIVE -> true
        Intensity.REST -> false
        else -> return null
    }
    val elapsedMillis = (lap.totalElapsedTime?.toDouble() ?: 0.0).toLong() * TimeUtils.MILLIS_PER_SECOND
    if (elapsedMillis <= 0L) return null
    val endMillis = lap.timestamp?.toEpochMillis()
    val startMillis = lap.startTime?.toEpochMillis() ?: endMillis?.minus(elapsedMillis) ?: return null
    return FitLap(
        isActive = isActive,
        startMillis = startMillis,
        endMillis = endMillis ?: (startMillis + elapsedMillis),
        elapsedMillis = elapsedMillis,
        distanceMeters = lap.totalDistance?.toDouble() ?: 0.0,
    )
}

private fun readRecord(record: RecordMesg): FitRecord? {
    val timestampMillis = record.timestamp?.toEpochMillis() ?: return null
    return FitRecord(
        timestampMillis = timestampMillis,
        heartRate = record.heartRate?.toInt()?.takeIf { it > 0 },
        cadence = record.cadence?.toInt()?.takeIf { it > 0 },
    )
}

private fun SessionMesg?.isRowingWorkout(): Boolean =
    this == null || subSport == SubSport.INDOOR_ROWING || sport == Sport.ROWING

private fun DateTime.toEpochMillis(): Long? =
    if (timestamp == DateTime.INVALID) null else getDate().time
