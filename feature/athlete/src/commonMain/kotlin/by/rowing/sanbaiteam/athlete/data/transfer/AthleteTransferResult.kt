package by.rowing.sanbaiteam.athlete.data.transfer

internal data class AthleteTransferResult(
    val athletesCreated: Int,
    val athletesUpdated: Int,
    val personalBestsImported: Int,
    val weightsImported: Int,
)