package by.rowing.sanbaiteam.training.data.chart.models

/** Длина одного сплита: по дистанции или по времени. */
internal sealed interface TrainingSplitLength {

    data class Distance(val meters: Int) : TrainingSplitLength

    data class Time(val millis: Long) : TrainingSplitLength
}