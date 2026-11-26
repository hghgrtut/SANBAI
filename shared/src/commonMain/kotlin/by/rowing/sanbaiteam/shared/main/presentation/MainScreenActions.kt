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

    fun openSyncDialog()

    fun dismissSyncDialog()

    fun changeSyncDateFrom(
        year: Int,
        month: Int,
        day: Int,
    )

    fun changeSyncDateTo(
        year: Int,
        month: Int,
        day: Int,
    )

    fun exportSyncBackup()

    fun onSyncExportPromptLaunched()

    fun onSyncExportToHandle(handle: String)

    fun onSyncImportFromHandle(handle: String)

    fun dismissSyncNotice()

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
            override fun openSyncDialog() {}
            override fun dismissSyncDialog() {}
            override fun changeSyncDateFrom(
                year: Int,
                month: Int,
                day: Int
            ) {
            }

            override fun changeSyncDateTo(
                year: Int,
                month: Int,
                day: Int
            ) {
            }

            override fun exportSyncBackup() {}
            override fun onSyncExportPromptLaunched() {}
            override fun onSyncExportToHandle(handle: String) {}
            override fun onSyncImportFromHandle(handle: String) {}
            override fun dismissSyncNotice() {}
        }
    }
}