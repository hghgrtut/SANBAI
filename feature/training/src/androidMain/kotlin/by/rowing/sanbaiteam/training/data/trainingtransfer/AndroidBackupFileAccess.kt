package by.rowing.sanbaiteam.training.data.trainingtransfer

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidBackupFileAccess(private val context: Context) : BackupFileAccess {

    override suspend fun write(
        handle: String,
        text: String
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(Uri.parse(handle))
                ?.use { stream -> stream.write(text.toByteArray()) }
                ?: throw IllegalStateException("Unable to open output stream")
        }.isSuccess
    }

    override suspend fun read(handle: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(handle))?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }
}
