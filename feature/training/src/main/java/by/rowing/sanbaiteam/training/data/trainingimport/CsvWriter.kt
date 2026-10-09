package by.rowing.sanbaiteam.training.data.trainingimport

import java.util.Locale

internal object CsvWriter {

    fun row(vararg cells: String?): String = cells.joinToString(",") { cell -> cell.orEmpty().escape() }

    fun Double.cell(): String = String.format(Locale.US, "%.1f", this)

    fun Int?.cell(): String = this?.toString().orEmpty()

    fun Long?.cell(): String = this?.toString().orEmpty()

    private fun String.escape(): String = if (any { symbol -> symbol in ESCAPED_SYMBOLS }) {
        "\"" + replace("\"", "\"\"") + "\""
    } else {
        this
    }

    private val ESCAPED_SYMBOLS = charArrayOf(',', '"', '\n', '\r')
}