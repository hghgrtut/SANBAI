package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.settings.data.repository.WorkDisplaySettingsRepository
import by.rowing.sanbaiteam.training.data.FakeTrainingDao
import by.rowing.sanbaiteam.training.data.FakeTrainingWorkDisplaySettingDao
import by.rowing.sanbaiteam.training.data.chart.TrainingChartReader
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportStorage

internal fun testTrainingRepository(
    trainings: List<TrainingEntity> = emptyList(),
    pieces: List<TrainingAthletePieceEntity> = emptyList(),
    athletesRepository: AthletesRepository = StubAthletesRepository(),
    importStorage: TrainingImportStorage = FakeTrainingImportStorage(),
    chartReader: TrainingChartReader = TrainingChartReader(),
    workDisplaySettingsRepository: WorkDisplaySettingsRepository =
        WorkDisplaySettingsRepository(FakeTrainingWorkDisplaySettingDao()),
): TrainingRepository = testTrainingRepository(
    dao = FakeTrainingDao(
        trainings = trainings,
        pieces = pieces,
    ),
    athletesRepository = athletesRepository,
    importStorage = importStorage,
    chartReader = chartReader,
    workDisplaySettingsRepository = workDisplaySettingsRepository,
)

internal fun testTrainingRepository(
    dao: TrainingDao,
    athletesRepository: AthletesRepository = StubAthletesRepository(),
    importStorage: TrainingImportStorage = FakeTrainingImportStorage(),
    chartReader: TrainingChartReader = TrainingChartReader(),
    workDisplaySettingsRepository: WorkDisplaySettingsRepository =
        WorkDisplaySettingsRepository(FakeTrainingWorkDisplaySettingDao()),
): TrainingRepository = TrainingRepository(
    dao = dao,
    athletesRepository = athletesRepository,
    importStorage = importStorage,
    chartReader = chartReader,
    workDisplaySettingsRepository = workDisplaySettingsRepository,
)
