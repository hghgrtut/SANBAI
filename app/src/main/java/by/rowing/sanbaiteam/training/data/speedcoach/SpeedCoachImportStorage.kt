package by.rowing.sanbaiteam.training.data.speedcoach

import android.content.Context
import java.io.File

internal class SpeedCoachImportStorage(
    private val context: Context
) {

    fun saveCsv(trainingId: Long, serial: String?, csvText: String): String {
        val dir = File(context.filesDir, "speedcoach_imports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val cleanSerial = (serial ?: "unknown").ifBlank { "unknown" }
        val fileName = "${trainingId}_${cleanSerial}_${System.currentTimeMillis()}.csv"
        val file = File(dir, fileName)
        file.writeText(csvText)
        return "speedcoach_imports/$fileName"
    }
}
