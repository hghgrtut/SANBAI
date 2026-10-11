package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal enum class TrainingImportSource(val storageFolder: String) {
    SPEED_COACH("speedcoach_imports"),
    CONCEPT2_FIT("concept2_imports"),
    RP3_CSV("rp3_imports");

    companion object {

        fun byCsvContent(csvText: String): TrainingImportSource = when {
            Concept2LapCsv.isArchive(csvText) -> CONCEPT2_FIT
            Rp3CsvParser.hasRp3CsvContent(csvText) -> RP3_CSV
            else -> SPEED_COACH
        }
    }
}

internal fun TrainingImportSource.impliedTrainingType(): TrainingPieceType = when (this) {
    TrainingImportSource.CONCEPT2_FIT -> TrainingPieceType.CONCEPT_ROW_ERG
    TrainingImportSource.SPEED_COACH -> TrainingPieceType.SINGLE
    TrainingImportSource.RP3_CSV -> TrainingPieceType.RP_3
}