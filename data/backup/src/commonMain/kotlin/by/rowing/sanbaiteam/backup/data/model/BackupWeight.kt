package by.rowing.sanbaiteam.backup.data.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.Serializable

@AllowDetektPublic
@Serializable
data class BackupWeight(
    val dateMillis: Long,
    val weightTenths: Int,
)