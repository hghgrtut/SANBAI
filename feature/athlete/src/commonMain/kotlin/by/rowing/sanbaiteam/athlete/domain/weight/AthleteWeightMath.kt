package by.rowing.sanbaiteam.athlete.domain.weight

import by.rowing.sanbaiteam.core.util.TimeUtils
import kotlin.math.roundToInt

internal object AthleteWeightMath {

    fun currentWeightTenths(
        records: List<AthleteWeightRecord>,
        todayMillis: Long,
    ): Int? {
        val today = TimeUtils.startOfDayMillis(timeInMillis = todayMillis)
        return sortedByDay(records = records).lastOrNull { it.dateMillis <= today }?.weightTenths
    }

    /**
     * Вес на дату.
     *
     * Если записи на эту дату нет, вес считается линейно между соседними записями
     * (день 1 — 91 кг, день 5 — 95 кг ⇒ день 2 — 92 кг). До первой записи и после
     * последней возвращается ближайшая крайняя запись.
     */
    fun weightAtTenths(
        records: List<AthleteWeightRecord>,
        dateMillis: Long,
    ): Int {
        val date = TimeUtils.startOfDayMillis(timeInMillis = dateMillis)
        val sorted = sortedByDay(records = records)
        val previous = sorted.lastOrNull { it.dateMillis <= date }
        val next = sorted.firstOrNull { it.dateMillis >= date }
        return when {
            previous == null -> next?.weightTenths
            next == null -> previous.weightTenths
            previous.dateMillis == next.dateMillis -> previous.weightTenths
            else -> interpolateWeightTenths(
                previous = previous,
                next = next,
                dateMillis = date,
            )
        } ?: 0
    }

    /** Средний вес по календарным дням. */
    fun averageWeightTenths(
        records: List<AthleteWeightRecord>,
        fromMillis: Long,
        toMillis: Long,
    ): Double? {
        val sorted = sortedByDay(records = records).ifEmpty { return null }
        val to = TimeUtils.startOfDayMillis(timeInMillis = toMillis)
        var day = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        var total = 0
        var days = 0
        while (day <= to) {
            total += weightAtTenths(
                records = sorted,
                dateMillis = day,
            )
            days++
            day = TimeUtils.plusDaysMillis(
                timeInMillis = day,
                days = 1,
            )
        }
        return total / days / TENTHS_IN_KILOGRAM
    }

    /** Только реальные записи внутри диапазона — график соединяет их линиями. */
    fun chartPoints(
        records: List<AthleteWeightRecord>,
        fromMillis: Long,
        toMillis: Long,
    ): List<AthleteWeightRecord> {
        val from = TimeUtils.startOfDayMillis(timeInMillis = fromMillis)
        val to = TimeUtils.startOfDayMillis(timeInMillis = toMillis)
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
        if (result.last() != lastOffset) {
            result += lastOffset
        }
        return result
    }

    private fun sortedByDay(records: List<AthleteWeightRecord>): List<AthleteWeightRecord> =
        records.map { record ->
            record.copy(dateMillis = TimeUtils.startOfDayMillis(timeInMillis = record.dateMillis))
        }.sortedBy { it.dateMillis }

    private fun interpolateWeightTenths(
        previous: AthleteWeightRecord,
        next: AthleteWeightRecord,
        dateMillis: Long,
    ): Int {
        val totalDays = TimeUtils.daysBetween(
            fromMillis = previous.dateMillis,
            toMillis = next.dateMillis,
        )
        if (totalDays <= 0) {
            return previous.weightTenths
        }
        val passedDays = TimeUtils.daysBetween(
            fromMillis = previous.dateMillis,
            toMillis = dateMillis,
        )
        val weight = previous.weightTenths +
                (next.weightTenths - previous.weightTenths).toDouble() * passedDays / totalDays
        return weight.roundToInt()
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