package by.rowing.sanbaiteam.training.data.trainingimport

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidTrainingFileReader(private val context: Context) : TrainingFileReader {

    override suspend fun readFiles(handles: List<String>): List<PickedImportFile> =
        withContext(Dispatchers.IO) {
            handles.mapNotNull { handle ->
                val uri = Uri.parse(handle)
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream -> stream.readBytes() }
                }.getOrNull() ?: return@mapNotNull null
                PickedImportFile(
                    bytes = bytes,
                    fileName = uri.readDisplayName()
                )
            }
        }

    private fun Uri.readDisplayName(): String? {
        val providerName = runCatching {
            context.contentResolver
                .query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                }
        }.getOrNull()
        return providerName?.takeIf { it.isNotBlank() } ?: lastPathSegment
    }
}
