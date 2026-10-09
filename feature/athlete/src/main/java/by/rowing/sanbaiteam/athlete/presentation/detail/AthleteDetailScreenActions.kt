package by.rowing.sanbaiteam.athlete.presentation.detail

import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal interface AthleteDetailScreenActions {

    fun onBackClick()

    fun onSaveClick()

    fun changeName(newName: String)

    fun changeBirthDate(newDate: String)

    fun changeGender(isMale: Boolean)

    fun changeSpeedCoachSerial(newSerial: String)

    fun addRecord(type: TrainingPieceType)

    fun removeRecord(
        type: TrainingPieceType,
        localId: Long,
    )

    fun changeRecordDistance(
        type: TrainingPieceType,
        localId: Long,
        text: String,
    )

    fun changeRecordTime(
        type: TrainingPieceType,
        localId: Long,
        text: String,
    )

    fun clearRecordsError()

    companion object {
        fun empty() = object : AthleteDetailScreenActions {
            override fun onBackClick() {}
            override fun onSaveClick() {}
            override fun changeName(newName: String) {}
            override fun changeBirthDate(newDate: String) {}
            override fun changeGender(isMale: Boolean) {}
            override fun changeSpeedCoachSerial(newSerial: String) {}
            override fun addRecord(type: TrainingPieceType) {}
            override fun removeRecord(
                type: TrainingPieceType,
                localId: Long
            ) {
            }
            override fun changeRecordDistance(
                type: TrainingPieceType,
                localId: Long,
                text: String
            ) {
            }
            override fun changeRecordTime(
                type: TrainingPieceType,
                localId: Long,
                text: String
            ) {
            }
            override fun clearRecordsError() {}
        }
    }
}
