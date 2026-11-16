package by.rowing.sanbaiteam.athlete.domain.weight

import by.rowing.sanbaiteam.core.util.TimeUtils

internal object AthleteWeightMath {

    fun currentWeightTenths(
        records: List<AthleteWeightRecord>,
        todayMillis: Long,
    ): Int? {
        val today = TimeUtils.startOfDayMillis(timeInMillis = todayMillis)
        return sortedByDay(records = records)
            .lastOrNull { it.dateMillis <= today }
            ?.weightTenths
    }

    /**
     * Последнее известное значение на дату (или первое, если записей до неё нет).
     * Заготовка для будущих расчётов эффективности тренировок по весу.
     */
    fun weightAtTenths(
        records: List<AthleteWeightRecord>,
        dateMillis: Long,
    ): Int? {
        val date = TimeUtils.startOfDayMillis(timeInMillis = dateMillis)
        val sorted = sortedByDay(records = records)
        return sorted.lastOrNull { it.dateMillis <= date }?.weightTenths
            ?: sorted.firstOrNull()?.weightTenths
    }

    /**
     * Средний вес по календарным дням.
     *
     * До первой записи берётся первое значение, после последней — последнее,
     * в дни между двумя записями — среднее двух соседних значений.
     */
    fun averageWeightTenths(
        records: List<AthleteWeightRecord>,
        fromMillis: Long,
        toMillis: Long,
    ): Double? {
        val sorted = sortedByDay(records = records)
        if (sorted.isEmpty()) return null
        val from = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        val to = TimeUtils.startOfDayMillis(timeInMillis = toMillis)
        if (to < from) return null

        var day = from
        var total = 0.0
        var days = 0
        while (day <= to) {
            total += weightAtDayTenths(
                records = sorted,
                dayMillis = day,
            )
            days++
            day = TimeUtils.plusDaysMillis(
                timeInMillis = day,
                days = 1,
            )
        }
        return total / days
    }

    /** Только реальные записи внутри диапазона — график соединяет их линиями. */
    fun chartPoints(
        records: List<AthleteWeightRecord>,
        fromMillis: Long,
        toMillis: Long,
    ): List<AthleteWeightRecord> {
        val from = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        val to = TimeUtils.startOfDayMillis(timeInMillis = toMillis)
        if (to < from) return emptyList()
        return sortedByDay(records = records)
            .filter { it.dateMillis in from..to }
            .map { record ->
                AthleteWeightRecord(
                    dateMillis = record.dateMillis,
                    weightTenths = record.weightTenths,
                )
            }
    }

    /** Смещения отметок дат от начала диапазона для оси времени графика. */
    fun dateTickOffsets(
        fromMillis: Long,
        toMillis: Long,
    ): List<Long> {
        val from = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        val to = TimeUtils.startOfDayMillis(timeInMillis = toMillis)
        val days = TimeUtils.daysBetween(
            fromMillis = from,
            toMillis = to,
        )
        if (days <= 0) return listOf(0L)

        val stepDays = tickStepDays(dayCount = days)
        val result = mutableListOf<Long>()
        var offsetDays = 0
        while (offsetDays <= days) {
            result += dayOffsetMillis(
                fromMillis = from,
                days = offsetDays,
            )
            offsetDays += stepDays
        }
        val lastOffset = dayOffsetMillis(
            fromMillis = from,
            days = days,
        )
        if (result.last() != lastOffset) result += lastOffset
        return result
    }

    private fun sortedByDay(records: List<AthleteWeightRecord>): List<AthleteWeightRecord> =
        records.map { record ->
            record.copy(dateMillis = TimeUtils.startOfDayMillis(timeInMillis = record.dateMillis))
        }.sortedBy { it.dateMillis }

    private fun weightAtDayTenths(
        records: List<AthleteWeightRecord>,
        dayMillis: Long,
    ): Double {
        val previous = records.lastOrNull { it.dateMillis <= dayMillis }
        val next = records.firstOrNull { it.dateMillis >= dayMillis }
        return when {
            previous == null -> next?.weightTenths?.toDouble() ?: 0.0
            next == null -> previous.weightTenths.toDouble()
            previous.dateMillis == next.dateMillis -> previous.weightTenths.toDouble()
            else -> (previous.weightTenths + next.weightTenths) / 2.0
        }
    }

    private fun tickStepDays(dayCount: Int): Int = when {
        dayCount <= 6 -> 1
        dayCount <= 14 -> 2
        dayCount <= 31 -> 5
        dayCount <= 92 -> 14
        dayCount <= 183 -> 30
        dayCount <= 366 -> 60
        else -> 120
    }

    private fun dayOffsetMillis(
        fromMillis: Long,
        days: Int,
    ): Long = TimeUtils.plusDaysMillis(
        timeInMillis = fromMillis,
        days = days,
    ) - fromMillis
}
