package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.models.FitLap

/**
 * Разбивает laps Concept2 на куски тренировки.
 *
 * PM5 режет непрерывную тренировку (just ride, single time/distance) на лапы, между которыми
 * нет отдыха. Если среди лапов нет ни одного отдыха, вся работа считается одним куском.
 * Если отдых есть, каждый активный лап остаётся отдельным отрезком (как раньше).
 */
internal fun List<FitLap>.concept2PieceRanges(): List<IntRange> = when {
    isEmpty() -> emptyList()
    all { it.isActive } -> listOf(indices)
    else -> indices.filter { this[it].isActive }.map { it..it }
}