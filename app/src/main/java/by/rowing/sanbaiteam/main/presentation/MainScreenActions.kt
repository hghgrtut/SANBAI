package by.rowing.sanbaiteam.main.presentation

internal interface MainScreenActions {

    fun navigateToAthletesScreen()

    companion object {

        fun empty() = object : MainScreenActions {

            override fun navigateToAthletesScreen() {}
        }
    }
}