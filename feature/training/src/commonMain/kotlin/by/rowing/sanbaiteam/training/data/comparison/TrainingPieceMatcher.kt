package by.rowing.sanbaiteam.training.data.comparison

import kotlin.math.abs

/**
 * Сопоставляет отрезки базовой и сравниваемой тренировок по длительности,
 * сохраняя их порядок. Применяется, когда количество отрезков различается.
 *
 * Отрезки считаются одинаковыми, если совпало время или дистанция с небольшим допуском.
 * Если полностью сопоставить меньшую тренировку не удалось, возвращается `null`,
 * и вызывающая сторона использует сравнение по порядковому номеру.
 */
internal object TrainingPieceMatcher {

    const val TIME_TOLERANCE_MILLIS = 5_000L
    const val DISTANCE_TOLERANCE_METERS = 5

    /**
     * Возвращает пары `order базового отрезка -> order сопоставленного отрезка`
     * или `null`, если сопоставить все отрезки меньшей тренировки не удалось.
     */
    fun match(
        base: List<PieceDurationKey>,
        compared: List<PieceDurationKey>,
    ): Map<Int, Int>? {
        val expectedMatches = minOf(base.size, compared.size)
        val matches = longestCommonSubsequence(
            base = base,
            compared = compared
        )
        return if (matches.size == expectedMatches) {
            matches.associate { (baseIndex, comparedIndex) -> base[baseIndex].order to compared[comparedIndex].order }
        } else {
            null
        }
    }

    private fun longestCommonSubsequence(
        base: List<PieceDurationKey>,
        compared: List<PieceDurationKey>,
    ): List<Pair<Int, Int>> {
        val lengths = Array(size = base.size + 1) { IntArray(size = compared.size + 1) }
        for (baseIndex in base.indices.reversed()) {
            for (comparedIndex in compared.indices.reversed()) {
                lengths[baseIndex][comparedIndex] = if (isSameDuration(base[baseIndex], compared[comparedIndex])) {
                    lengths[baseIndex + 1][comparedIndex + 1] + 1
                } else {
                    maxOf(lengths[baseIndex + 1][comparedIndex], lengths[baseIndex][comparedIndex + 1])
                }
            }
        }

        val matches = mutableListOf<Pair<Int, Int>>()
        var baseIndex = 0
        var comparedIndex = 0
        while (baseIndex < base.size && comparedIndex < compared.size) {
            when {
                isSameDuration(base[baseIndex], compared[comparedIndex]) -> {
                    matches += baseIndex to comparedIndex
                    baseIndex++
                    comparedIndex++
                }

                lengths[baseIndex + 1][comparedIndex] >= lengths[baseIndex][comparedIndex + 1] -> baseIndex++
                else -> comparedIndex++
            }
        }
        return matches
    }

    private fun isSameDuration(
        base: PieceDurationKey,
        compared: PieceDurationKey,
    ): Boolean = abs(base.timeMillis - compared.timeMillis) <= TIME_TOLERANCE_MILLIS ||
        abs(base.distanceMeters - compared.distanceMeters) <= DISTANCE_TOLERANCE_METERS
}
