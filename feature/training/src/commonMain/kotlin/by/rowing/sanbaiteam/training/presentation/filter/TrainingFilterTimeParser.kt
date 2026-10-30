package by.rowing.sanbaiteam.training.presentation.filter

import by.rowing.sanbaiteam.core.util.TimeUtils

/**
 * Разбирает границу диапазона времени отрезка.
 *
 * Поддерживаются только два формата:
 * - `минуты:секунды`, например `1:30`;
 * - `минуты`, например `2` (трактуется как 2 минуты).
 *
 * Десятые доли секунды не поддерживаются. Возвращает миллисекунды или `null`,
 * если ввод некорректен.
 */
internal object TrainingFilterTimeParser {

    private const val SEPARATOR = ':'

    fun parseMillis(input: String): Long? {
        val value = input.trim()
        if (value.isEmpty()) return null

        val parts = value.split(SEPARATOR)
        return when (parts.size) {
            1 -> parseMinutes(minutes = parts.single())
            2 -> parseMinutesWithSeconds(
                minutes = parts[0],
                seconds = parts[1]
            )

            else -> null
        }
    }

    private fun parseMinutes(minutes: String): Long? {
        val parsedMinutes = minutes.toNonNegativeLong() ?: return null
        return parsedMinutes * TimeUtils.SECONDS_PER_MINUTE * TimeUtils.MILLIS_PER_SECOND
    }

    private fun parseMinutesWithSeconds(
        minutes: String,
        seconds: String
    ): Long? {
        val millisFromMinutes = parseMinutes(minutes = minutes) ?: return null
        val parsedSeconds = seconds.toNonNegativeLong().takeIf { it != null && it < TimeUtils.SECONDS_PER_MINUTE }
            ?: return null
        return millisFromMinutes + parsedSeconds * TimeUtils.MILLIS_PER_SECOND
    }

    private fun String.toNonNegativeLong(): Long? =
        toLongOrNull()?.takeIf { it >= 0 }
}