package by.rowing.sanbaiteam.main.presentation

internal interface MainScreenActions {

    fun navigateToAthletesScreen()

    fun navigateToTrainingsScreen()

    companion object {

        fun empty() = object : MainScreenActions {

            override fun navigateToAthletesScreen() {}
            override fun navigateToTrainingsScreen() {}
        }
    }
}