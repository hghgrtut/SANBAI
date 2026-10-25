package by.rowing.sanbaiteam.training.data.trainingimport

internal object TrainingImportParsers {

    private val parsers: List<TrainingImportParser> = listOf(
        Concept2FitParser,
        SpeedCoachFitParser,
        Rp3CsvParser,
    )

    fun detect(bytes: ByteArray): TrainingImportParser? = parsers.firstOrNull { parser ->
        parser.matches(TrainingImportContainer.unpackOrSingle(bytes).first())
    }

    fun bySource(source: TrainingImportSource): TrainingImportParser = parsers.first { parser ->
        parser.source == source
    }
}