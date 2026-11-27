package by.rowing.sanbaiteam.backup.domain.model

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

@AllowDetektPublic
data class BackupImportResult(
    val trainingsImported: Int = 0,
    val trainingsSkippedAsDuplicates: Int = 0,
    val athletesCreated: Int = 0,
    val athletesUpdated: Int = 0,
    val weightsImported: Int = 0,
)