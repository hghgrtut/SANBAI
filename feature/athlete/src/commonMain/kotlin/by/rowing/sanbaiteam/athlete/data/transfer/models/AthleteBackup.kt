package by.rowing.sanbaiteam.athlete.data.transfer.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AthleteBackup(
    val kind: String,
    val formatVersion: Int,
    val athletes: List<BackupAthlete> = emptyList(),
) {

    companion object {

        const val KIND = "sanbai-athletes"
        const val FORMAT_VERSION = 1
    }
}