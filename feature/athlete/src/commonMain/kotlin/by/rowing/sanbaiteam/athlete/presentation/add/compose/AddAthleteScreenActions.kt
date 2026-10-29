package by.rowing.sanbaiteam.athlete.presentation.add.compose

import by.rowing.sanbaiteam.core.presentation.compose.NavigateBackAware

internal interface AddAthleteScreenActions : NavigateBackAware {

    fun onSaveClick()

    fun changeName(newName: String)

    fun changeBirthDate(newDate: String)

    fun changeSpeedCoachSerial(newSerial: String)

    fun changeGender(isMale: Boolean)

    companion object {

        fun empty(): AddAthleteScreenActions =
            object : AddAthleteScreenActions, NavigateBackAware by NavigateBackAware.empty() {

                override fun onSaveClick() {}
                override fun changeName(newName: String) {}
                override fun changeBirthDate(newDate: String) {}
                override fun changeSpeedCoachSerial(newSerial: String) {}
                override fun changeGender(isMale: Boolean) {}
            }
    }
}