package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal enum class TrainingImportSource(val storageFolder: String) {
    SPEED_COACH("speedcoach_imports"),
    CONCEPT2_FIT("concept2_imports");

    companion object {

        fun byCsvContent(csvText: String): TrainingImportSource =
            if (Concept2LapCsv.isArchive(csvText)) CONCEPT2_FIT else SPEED_COACH
    }
}

internal fun TrainingImportSource.impliedTrainingType(): TrainingPieceType = when (this) {
    TrainingImportSource.CONCEPT2_FIT -> TrainingPieceType.CONCEPT_ROW_ERG
    TrainingImportSource.SPEED_COACH -> TrainingPieceType.SINGLE
}