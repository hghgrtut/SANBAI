package by.rowing.sanbaiteam.athlete.presentation.detail.state

internal data class PersonalBestDraft(
    val localId: Long = nextLocalId(),
    val distanceText: String = "",
    val timeText: String = "",
    val distanceMeters: Int = 0,
    val timeMillis: Long = 0,
) {

    companion object {

        private var localIdSeq = 1L

        fun nextLocalId(): Long = localIdSeq++
    }
}