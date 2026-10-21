package by.rowing.sanbaiteam.shared.main.presentation

internal interface MainScreenActions {

    fun navigateToAthletesScreen()

    fun navigateToTrainingsScreen()

    fun navigateToCalculatorScreen()

    fun navigateToAddTrainingScreen()

    fun onTrainingClick(trainingId: Long)

    companion object {

        fun empty() = object : MainScreenActions {

            override fun navigateToAthletesScreen() {}
            override fun navigateToTrainingsScreen() {}
            override fun navigateToCalculatorScreen() {}
            override fun navigateToAddTrainingScreen() {}
            override fun onTrainingClick(trainingId: Long) {}
        }
    }
}
