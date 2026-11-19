package by.rowing.sanbaiteam.athlete.data.transfer.models

import kotlinx.serialization.Serializable

@Serializable
internal data class BackupWeight(
    val dateMillis: Long,
    val weightTenths: Int,
)