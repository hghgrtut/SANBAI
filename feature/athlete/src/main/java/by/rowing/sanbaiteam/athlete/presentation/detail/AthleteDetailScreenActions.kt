package by.rowing.sanbaiteam.athlete.presentation.detail

internal interface AthleteDetailScreenActions {

    fun onBackClick()

    fun onSaveClick()

    fun changeName(newName: String)

    fun changeBirthDate(newDate: String)

    fun changeGender(isMale: Boolean)

    fun changeSpeedCoachSerial(newSerial: String)

    fun addRecord()

    fun removeRecord(localId: Long)

    fun changeRecordDistance(localId: Long, text: String)

    fun changeRecordTime(localId: Long, text: String)

    fun clearRecordsError()

    companion object {
        fun empty() = object : AthleteDetailScreenActions {
            override fun onBackClick() {}
            override fun onSaveClick() {}
            override fun changeName(newName: String) {}
            override fun changeBirthDate(newDate: String) {}
            override fun changeGender(isMale: Boolean) {}
            override fun changeSpeedCoachSerial(newSerial: String) {}
            override fun addRecord() {}
            override fun removeRecord(localId: Long) {}
            override fun changeRecordDistance(localId: Long, text: String) {}
            override fun changeRecordTime(localId: Long, text: String) {}
            override fun clearRecordsError() {}
        }
    }
}
