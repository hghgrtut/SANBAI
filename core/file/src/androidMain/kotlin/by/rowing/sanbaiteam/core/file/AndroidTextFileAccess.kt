package by.rowing.sanbaiteam.core.file

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidTextFileAccess(private val context: Context) : TextFileAccess {

    override suspend fun write(
        handle: String,
        text: String,
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(handle.toUri())
                ?.use { stream -> stream.write(text.toByteArray()) }
                ?: throw IllegalStateException("Unable to open output stream")
        }.isSuccess
    }

    override suspend fun read(handle: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(handle.toUri())?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }
}