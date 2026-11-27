package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
enum class TrainingImportSource(val storageFolder: String) {
    SPEED_COACH("speedcoach_imports"),
    CONCEPT2_FIT("concept2_imports"),
    RP3_CSV("rp3_imports")
}