package by.rowing.sanbaiteam.training.data.trainingtransfer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class TrainingBackup(
    val formatVersion: Int = FORMAT_VERSION,
    val athletes: List<BackupAthlete> = emptyList(),
    val trainings: List<BackupTraining> = emptyList(),
) {
    companion object {
        const val FORMAT_VERSION = 1
    }
}

@Serializable
internal data class BackupAthlete(
    val name: String,
    val dateOfBirthMillis: Long,
    val isMale: Boolean,
)

@Serializable
internal data class BackupTraining(
    val dateMillis: Long,
    val type: String,
    val sourceDeviceSerial: String? = null,
    val sourceSessionName: String? = null,
    val sourceCsvContent: String? = null,
    val pieces: List<BackupPiece> = emptyList(),
)

@Serializable
internal data class BackupPiece(
    val athleteIndex: Int,
    val order: Int,
    val distanceMeters: Int,
    val timeMillis: Long,
    val strokeRate: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val recoveryHeartRate: Int? = null,
)

internal object TrainingBackupSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun encode(backup: TrainingBackup): String = json.encodeToString(backup)

    fun decode(text: String): TrainingBackup = json.decodeFromString(text)
}