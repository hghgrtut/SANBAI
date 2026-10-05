package by.rowing.sanbaiteam.training.data.trainingimport

internal object TrainingImportParsers {

    private val parsers: List<TrainingImportParser> = listOf(
        Concept2FitParser,
        SpeedCoachCsvParser,
    )

    fun detect(bytes: ByteArray): TrainingImportParser? = parsers.firstOrNull { parser ->
        parser.matches(bytes)
    }

    fun bySource(source: TrainingImportSource): TrainingImportParser = parsers.first { parser ->
        parser.source == source
    }
}