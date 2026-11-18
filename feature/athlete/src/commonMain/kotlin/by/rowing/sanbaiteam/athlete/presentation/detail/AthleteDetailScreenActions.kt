package by.rowing.sanbaiteam.athlete.presentation.detail

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType

internal interface AthleteDetailScreenActions : NavigateBackAware {

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

    fun openWeightDialog()

    fun dismissWeightDialog()

    fun changeWeightDate(
        year: Int,
        month: Int,
        day: Int,
    )

    fun changeWeightValue(text: String)

    fun saveWeight()

    fun requestDeleteWeight(dateMillis: Long)

    fun dismissDeleteWeight()

    fun confirmDeleteWeight()

    fun toggleWeightTable()

    fun openWeightRangeDialog()

    fun dismissWeightRangeDialog()

    fun changeWeightRangeFrom(
        year: Int,
        month: Int,
        day: Int,
    )

    fun changeWeightRangeTo(
        year: Int,
        month: Int,
        day: Int,
    )

    fun buildWeightChart()

    companion object {

        fun empty(): AthleteDetailScreenActions =
            object : AthleteDetailScreenActions, NavigateBackAware by NavigateBackAware.empty() {

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
                override fun openWeightDialog() {}
                override fun dismissWeightDialog() {}
                override fun changeWeightDate(
                    year: Int,
                    month: Int,
                    day: Int
                ) {
                }

                override fun changeWeightValue(text: String) {}
                override fun saveWeight() {}
                override fun requestDeleteWeight(dateMillis: Long) {}
                override fun dismissDeleteWeight() {}
                override fun confirmDeleteWeight() {}
                override fun toggleWeightTable() {}
                override fun openWeightRangeDialog() {}
                override fun dismissWeightRangeDialog() {}
                override fun changeWeightRangeFrom(
                    year: Int,
                    month: Int,
                    day: Int
                ) {
                }

                override fun changeWeightRangeTo(
                    year: Int,
                    month: Int,
                    day: Int
                ) {
                }

                override fun buildWeightChart() {}
            }
    }
}
