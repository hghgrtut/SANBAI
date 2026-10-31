package by.rowing.sanbaiteam.training.di

import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboard
import by.rowing.sanbaiteam.training.data.dashboard.TrainingDashboardImpl
import by.rowing.sanbaiteam.training.data.repository.SavedTrainingFiltersRepository
import by.rowing.sanbaiteam.training.data.repository.TrainingRepository
import by.rowing.sanbaiteam.training.data.trainingtransfer.TrainingTransferRepository
import by.rowing.sanbaiteam.training.presentation.add.AddTrainingViewModel
import by.rowing.sanbaiteam.training.presentation.chart.TrainingChartViewModel
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingCompareViewModel
import by.rowing.sanbaiteam.training.presentation.comparison.TrainingComparisonSelectionStore
import by.rowing.sanbaiteam.training.presentation.detail.TrainingDetailViewModel
import by.rowing.sanbaiteam.training.presentation.list.ListTrainingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val trainingModule = module {
    includes(trainingPlatformModule)

    singleOf(::TrainingChartReader)
    singleOf(::TrainingRepository)
    singleOf(::SavedTrainingFiltersRepository)
    singleOf(::TrainingTransferRepository)
    singleOf(::TrainingComparisonSelectionStore)
    single<TrainingDashboard> { TrainingDashboardImpl(repository = get()) }

    viewModelOf(::ListTrainingViewModel)
    viewModelOf(::TrainingDetailViewModel)
    viewModelOf(::TrainingCompareViewModel)
    viewModelOf(::TrainingChartViewModel)
    viewModel {
        AddTrainingViewModel(
            athleteRepository = get(),
            trainingRepository = get(),
            trainingImportStorage = get(),
            trainingFileReader = get(),
            composeNavigator = get(),
            importUri = getOrNull()
        )
    }
}