package by.rowing.sanbaiteam.training.data.trainingtransfer

internal class InMemoryBackupFileAccess : BackupFileAccess {

    private val files = mutableMapOf<String, String>()

    override suspend fun write(
        handle: String,
        text: String
    ): Boolean {
        files[handle] = text
        return true
    }

    override suspend fun read(handle: String): String? = files[handle]
}
