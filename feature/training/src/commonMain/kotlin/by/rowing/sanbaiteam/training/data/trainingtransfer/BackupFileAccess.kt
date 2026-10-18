package by.rowing.sanbaiteam.training.data.trainingtransfer

internal interface BackupFileAccess {

    suspend fun write(
        handle: String,
        text: String
    ): Boolean

    suspend fun read(handle: String): String?
}
