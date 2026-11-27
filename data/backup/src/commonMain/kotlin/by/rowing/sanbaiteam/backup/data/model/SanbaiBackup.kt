package by.rowing.sanbaiteam.backup.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.Serializable

@AllowDetektPublic
@Serializable
data class SanbaiBackup(
    val kind: String = KIND,
    val formatVersion: Int = FORMAT_VERSION,
    val exportedAtMillis: Long = 0L,
    val fromMillis: Long? = null,
    val toMillis: Long? = null,
    val athletes: List<BackupAthlete> = emptyList(),
    val trainings: List<BackupTraining> = emptyList(),
) {

    companion object {

        internal const val KIND = "sanbai-backup"
        internal const val FORMAT_VERSION = 1
    }
}