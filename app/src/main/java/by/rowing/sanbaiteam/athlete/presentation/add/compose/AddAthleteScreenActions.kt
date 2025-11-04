package by.rowing.sanbaiteam.athlete.presentation.add.compose

internal interface AddAthleteScreenActions {

    fun onBackClick()

    fun onSaveClick()

    fun changeName(newName: String)

    fun changeBirthDate(newDate: String)

    fun changeGender(isMale: Boolean)

    companion object {

        fun empty() = object : AddAthleteScreenActions {

            override fun onBackClick() {}
            override fun onSaveClick() {}
            override fun changeName(newName: String) {}
            override fun changeBirthDate(newDate: String) {}
            override fun changeGender(isMale: Boolean) {}
        }
    }
}