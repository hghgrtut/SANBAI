package by.rowing.sanbaiteam.shared.main.presentation

internal interface MainScreenActions {

    fun navigateToAthletesScreen()

    fun navigateToTrainingsScreen()

    fun navigateToCalculatorScreen()

    fun navigateToAddTrainingScreen()

    fun navigateToSettings()

    fun onTrainingClick(trainingId: Long)

    fun openWeightDialog()

    fun dismissWeightDialog()

    fun changeWeightAthlete(athleteId: Long)

    fun changeWeightDate(
        year: Int,
        month: Int,
        day: Int,
    )

    fun changeWeightValue(text: String)

    fun saveWeight()

    companion object {

        fun empty() = object : MainScreenActions {

            override fun navigateToAthletesScreen() {}
            override fun navigateToTrainingsScreen() {}
            override fun navigateToCalculatorScreen() {}
            override fun navigateToAddTrainingScreen() {}
            override fun navigateToSettings() {}
            override fun onTrainingClick(trainingId: Long) {}
            override fun openWeightDialog() {}
            override fun dismissWeightDialog() {}
            override fun changeWeightAthlete(athleteId: Long) {}
            override fun changeWeightDate(
                year: Int,
                month: Int,
                day: Int
            ) {
            }

            override fun changeWeightValue(text: String) {}
            override fun saveWeight() {}
        }
    }
}
