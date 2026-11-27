package by.rowing.sanbaiteam.backup.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.Serializable

@AllowDetektPublic
@Serializable
data class BackupTraining(
    val dateMillis: Long,
    val type: String,
    val source: String? = null,
    val sourceFileBase64: String? = null,
    val sourceFileExtension: String? = null,
    val pieces: List<BackupPiece> = emptyList(),
)