package by.rowing.sanbaiteam.backup

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthleteWeightEntity
import by.rowing.sanbaiteam.backup.data.model.BackupAthlete
import by.rowing.sanbaiteam.backup.data.model.BackupPiece
import by.rowing.sanbaiteam.backup.data.model.BackupTraining
import by.rowing.sanbaiteam.backup.data.model.BackupWeight
import by.rowing.sanbaiteam.backup.data.model.SanbaiBackup
import by.rowing.sanbaiteam.backup.data.repository.BackupRepository
import by.rowing.sanbaiteam.backup.data.serializer.BackupSerializer
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class BackupRepositoryTest {

    private val day1 = TimeUtils.startOfDayMillis(timeInMillis = 1_700_000_000_000L)
    private val day2 = day1 + TimeUtils.MILLIS_PER_DAY
    private val day3 = day2 + TimeUtils.MILLIS_PER_DAY
    private val dayOutside = day1 - 10 * TimeUtils.MILLIS_PER_DAY
    private val serializer = BackupSerializer()

    @Test
    fun `export unified backup includes period data and mentioned athletes only`() = runTest {
        val athlete1 = AthleteEntity(
            id = 1,
            name = "Иванов Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = null
        )
        val athlete2 = AthleteEntity(
            id = 2,
            name = "Петров Пётр",
            dateOfBirth = day2,
            isMale = true,
            speedCoachSerial = null
        )
        val athlete3 = AthleteEntity(
            id = 3,
            name = "Сидоров Сидор",
            dateOfBirth = day3,
            isMale = true,
            speedCoachSerial = null
        )
        val env = env(
            athletes = listOf(athlete1, athlete2, athlete3),
            trainings = listOf(
                TrainingEntity(
                    id = 10,
                    dateMillis = day2 + 3_600_000L,
                    type = TrainingPieceType.SINGLE
                ),
                TrainingEntity(
                    id = 11,
                    dateMillis = dayOutside + 3_600_000L,
                    type = TrainingPieceType.SINGLE
                ),
            ),
            pieces = listOf(
                piece(
                    trainingId = 10,
                    athleteId = 1
                ),
                piece(
                    trainingId = 11,
                    athleteId = 3
                )
            ),
            weights = listOf(
                AthleteWeightEntity(
                    athleteId = 2,
                    dateMillis = TimeUtils.noonOfDay(day2),
                    weightTenths = 800
                ),
                AthleteWeightEntity(
                    athleteId = 2,
                    dateMillis = TimeUtils.noonOfDay(dayOutside),
                    weightTenths = 810
                ),
            ),
        )

        val backup = env.repository.exportUnifiedBackup(fromMillis = day1, toMillis = day3)

        assertEquals(1, backup.trainings.size)
        assertEquals(day2 + 3_600_000L, backup.trainings.single().dateMillis)
        assertEquals(setOf("Иванов Иван", "Петров Пётр"), backup.athletes.map { it.name }.toSet())
        val petrov = backup.athletes.first { it.name == "Петров Пётр" }
        assertEquals(listOf(800), petrov.weights.map { it.weightTenths })
    }

    @Test
    fun `export and import round trip restores training source file`() = runTest {
        val athlete = AthleteEntity(
            id = 1, name = "Иванов Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = null
        )
        val sourceBytes = byteArrayOf(1, 2, 3, 4)
        val exportStorage = FakeTrainingImportStorage().apply {
            savedFiles["speedcoach_imports/10_unknown.fit"] = sourceBytes
        }
        val exportEnv = env(
            athletes = listOf(athlete),
            trainings = listOf(
                TrainingEntity(
                    id = 10,
                    dateMillis = day2,
                    type = TrainingPieceType.SINGLE,
                    sourceFileRelativePath = "speedcoach_imports/10_unknown.fit",
                )
            ),
            pieces = listOf(
                piece(
                    trainingId = 10,
                    athleteId = 1,
                    sourceFileIndex = 0,
                    sourceIntervalIndex = 0
                )
            ),
            storage = exportStorage,
        )

        val exported = exportEnv.repository.exportUnifiedBackup(
            fromMillis = day1,
            toMillis = day3
        )

        assertEquals(Base64.encode(sourceBytes), exported.trainings.single().sourceFileBase64)

        val importEnv = env()
        val result = importEnv.repository.import(serializer.encode(exported))

        assertEquals(1, result.trainingsImported)
        assertTrue(sourceBytes.contentEquals(importEnv.storage.savedFiles.values.single()))
    }

    @Test
    fun `import rejects old formats`() = runTest {
        val env = env()

        assertFailsWith<IllegalArgumentException> {
            env.repository.import("""{"kind":"sanbai-athletes","formatVersion":1,"athletes":[]}""")
        }
        assertFailsWith<IllegalArgumentException> {
            env.repository.import("""{"formatVersion":2,"athletes":[],"trainings":[]}""")
        }
    }

    @Test
    fun `import matches athlete by name and dob and upserts weights`() = runTest {
        val existing = AthleteEntity(
            id = 5,
            name = "Иванов  Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = "SC1"
        )
        val env = env(
            athletes = listOf(existing),
            weights = listOf(
                AthleteWeightEntity(
                    athleteId = 5,
                    dateMillis = TimeUtils.noonOfDay(day2),
                    weightTenths = 700
                )
            ),
        )
        val backup = SanbaiBackup(
            athletes = listOf(
                BackupAthlete(
                    name = "иванов иван",
                    dateOfBirthMillis = day1 + 12 * 60 * 60 * 1000L,
                    isMale = false,
                    weights = listOf(
                        BackupWeight(
                            dateMillis = day2,
                            weightTenths = 750
                        ),
                        BackupWeight(
                            dateMillis = day3,
                            weightTenths = 760
                        ),
                    ),
                )
            ),
        )

        val result = env.repository.import(serializer.encode(backup))

        assertEquals(0, result.athletesCreated)
        assertEquals(1, result.athletesUpdated)
        assertEquals(2, result.weightsImported)
        val updated = env.athletesRepository.athletes.single()
        assertEquals(5, updated.id)
        assertEquals("Иванов  Иван", updated.name)
        assertEquals(true, updated.isMale)
        assertEquals("SC1", updated.speedCoachSerial)
        val weights = env.weightDao.weights.filter { it.athleteId == 5L }.sortedBy { it.dateMillis }
        assertEquals(listOf(750, 760), weights.map { it.weightTenths })
    }

    @Test
    fun `import skips duplicate training and reimport is idempotent`() = runTest {
        val athlete = AthleteEntity(
            id = 1,
            name = "Иванов Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = null
        )
        val storage = FakeTrainingImportStorage().apply {
            savedFiles["speedcoach_imports/10_a.fit"] = byteArrayOf(1, 2, 3)
        }
        val env = env(
            athletes = listOf(athlete),
            trainings = listOf(
                TrainingEntity(
                    id = 10,
                    dateMillis = day2 + 1000L,
                    type = TrainingPieceType.SINGLE,
                    sourceFileRelativePath = "speedcoach_imports/10_a.fit",
                )
            ),
            pieces = listOf(
                piece(
                    trainingId = 10,
                    athleteId = 1,
                    distanceMeters = 2000,
                    timeMillis = 400_000L,
                    strokeRate = 30.0
                )
            ),
            storage = storage,
        )
        val backup = SanbaiBackup(
            athletes = listOf(
                BackupAthlete(
                    name = "Иванов Иван",
                    dateOfBirthMillis = day1,
                    isMale = true
                )
            ),
            trainings = listOf(
                BackupTraining(
                    dateMillis = day2 + 2000L,
                    type = "SINGLE",
                    source = "SPEED_COACH",
                    sourceFileBase64 = Base64.encode(byteArrayOf(1, 2, 3)),
                    sourceFileExtension = "fit",
                    pieces = listOf(
                        backupPiece(
                            athleteIndex = 0,
                            distanceMeters = 2000,
                            timeMillis = 400_000L,
                            strokeRate = 30.0
                        )
                    ),
                )
            ),
        )

        val first = env.repository.import(serializer.encode(backup))
        val second = env.repository.import(serializer.encode(backup))

        assertEquals(0, first.trainingsImported)
        assertEquals(1, first.trainingsSkippedAsDuplicates)
        assertEquals(0, second.trainingsImported)
        assertEquals(1, second.trainingsSkippedAsDuplicates)
        assertEquals(1, env.trainingDao.trainings.size)
    }

    @Test
    fun `different date athlete piece or source bytes is not duplicate`() = runTest {
        fun baseEnv(): TestEnv {
            val athlete1 = AthleteEntity(
                id = 1,
                name = "Иванов Иван",
                dateOfBirth = day1,
                isMale = true,
                speedCoachSerial = null
            )
            val athlete2 = AthleteEntity(
                id = 2,
                name = "Петров Пётр",
                dateOfBirth = day1,
                isMale = true,
                speedCoachSerial = null
            )
            val storage = FakeTrainingImportStorage().apply {
                savedFiles["speedcoach_imports/10_a.fit"] = byteArrayOf(1, 2, 3)
            }
            return env(
                athletes = listOf(athlete1, athlete2),
                trainings = listOf(
                    TrainingEntity(
                        id = 10,
                        dateMillis = day2,
                        type = TrainingPieceType.SINGLE,
                        sourceFileRelativePath = "speedcoach_imports/10_a.fit",
                    )
                ),
                pieces = listOf(
                    piece(
                        trainingId = 10,
                        athleteId = 1,
                        distanceMeters = 2000,
                        timeMillis = 400_000L,
                        strokeRate = 30.0
                    )
                ),
                storage = storage,
            )
        }

        val baseTraining = BackupTraining(
            dateMillis = day2,
            type = "SINGLE",
            source = "SPEED_COACH",
            sourceFileBase64 = Base64.encode(byteArrayOf(1, 2, 3)),
            sourceFileExtension = "fit",
            pieces = listOf(
                backupPiece(
                    athleteIndex = 0,
                    distanceMeters = 2000,
                    timeMillis = 400_000L,
                    strokeRate = 30.0
                )
            ),
        )
        val backup = SanbaiBackup(
            athletes = listOf(
                BackupAthlete(
                    name = "Иванов Иван",
                    dateOfBirthMillis = day1,
                    isMale = true
                ),
                BackupAthlete(
                    name = "Петров Пётр",
                    dateOfBirthMillis = day1,
                    isMale = true
                ),
            ),
            trainings = listOf(baseTraining),
        )

        assertImported(baseEnv(), backup.copy(trainings = listOf(baseTraining.copy(dateMillis = day3))))
        assertImported(
            baseEnv(),
            backup.copy(
                trainings = listOf(
                    baseTraining.copy(
                        pieces = listOf(
                            backupPiece(
                                athleteIndex = 1,
                                distanceMeters = 2000,
                                timeMillis = 400_000L,
                                strokeRate = 30.0
                            )
                        )
                    )
                )
            ),
        )
        assertImported(
            baseEnv(),
            backup.copy(
                trainings = listOf(
                    baseTraining.copy(
                        pieces = listOf(
                            backupPiece(
                                athleteIndex = 0,
                                distanceMeters = 2001,
                                timeMillis = 400_000L,
                                strokeRate = 30.0
                            )
                        )
                    )
                )
            ),
        )
        assertImported(
            baseEnv(),
            backup.copy(trainings = listOf(baseTraining.copy(sourceFileBase64 = Base64.encode(byteArrayOf(9, 9, 9))))),
        )
    }

    @Test
    fun `missing source on both sides can be duplicate`() = runTest {
        val athlete = AthleteEntity(
            id = 1,
            name = "Иванов Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = null
        )
        val env = env(
            athletes = listOf(athlete),
            trainings = listOf(
                TrainingEntity(
                    id = 10,
                    dateMillis = day2,
                    type = TrainingPieceType.SINGLE,
                    sourceFileRelativePath = null
                )
            ),
            pieces = listOf(
                piece(
                    trainingId = 10,
                    athleteId = 1
                )
            ),
        )
        val backup = SanbaiBackup(
            athletes = listOf(
                BackupAthlete(
                    name = "Иванов Иван",
                    dateOfBirthMillis = day1,
                    isMale = true
                )
            ),
            trainings = listOf(
                BackupTraining(
                    dateMillis = day2,
                    type = "SINGLE",
                    pieces = listOf(backupPiece(athleteIndex = 0))
                )
            ),
        )

        val result = env.repository.import(serializer.encode(backup))

        assertEquals(0, result.trainingsImported)
        assertEquals(1, result.trainingsSkippedAsDuplicates)
    }

    @Test
    fun `export selected trainings and athletes use new format`() = runTest {
        val athlete = AthleteEntity(
            id = 1,
            name = "Иванов Иван",
            dateOfBirth = day1,
            isMale = true,
            speedCoachSerial = null
        )
        val env = env(
            athletes = listOf(athlete),
            trainings = listOf(
                TrainingEntity(
                    id = 10,
                    dateMillis = day2,
                    type = TrainingPieceType.SINGLE
                )
            ),
            pieces = listOf(
                piece(
                    trainingId = 10,
                    athleteId = 1
                )
            ),
            weights = listOf(
                AthleteWeightEntity(
                    athleteId = 1,
                    dateMillis = TimeUtils.noonOfDay(day2),
                    weightTenths = 750
                )
            ),
        )

        val trainingsBackup = env.repository.exportSelectedTrainings(setOf(10L))
        val athletesBackup = env.repository.exportSelectedAthletes(setOf(1L))

        assertEquals(SanbaiBackup.KIND, trainingsBackup.kind)
        assertEquals(1, trainingsBackup.trainings.size)
        assertEquals(1, trainingsBackup.athletes.size)
        assertEquals(SanbaiBackup.KIND, athletesBackup.kind)
        assertEquals(0, athletesBackup.trainings.size)
        assertEquals(750, athletesBackup.athletes.single().weights.single().weightTenths)
    }

    private suspend fun assertImported(env: TestEnv, backup: SanbaiBackup) {
        val result = env.repository.import(serializer.encode(backup))
        assertEquals(1, result.trainingsImported, "Expected import for $backup")
    }

    private fun env(
        athletes: List<AthleteEntity> = emptyList(),
        trainings: List<TrainingEntity> = emptyList(),
        pieces: List<TrainingAthletePieceEntity> = emptyList(),
        weights: List<AthleteWeightEntity> = emptyList(),
        storage: FakeTrainingImportStorage = FakeTrainingImportStorage(),
        now: Long = day3,
    ): TestEnv {
        val sharedWeights = weights.toMutableList()
        val trainingDao = FakeTrainingDao(
            trainings = trainings,
            pieces = pieces
        )
        val athletesRepository = FakeAthletesRepository(
            initial = athletes,
            weights = sharedWeights
        )
        val weightDao = FakeAthleteWeightDao(weights = sharedWeights)
        return TestEnv(
            trainingDao = trainingDao,
            athletesRepository = athletesRepository,
            weightDao = weightDao,
            storage = storage,
            repository = BackupRepository(
                trainingDao = trainingDao,
                athletesRepository = athletesRepository,
                athleteWeightDao = weightDao,
                importStorage = storage,
                backupSerializer = serializer,
                nowMillis = { now },
            ),
        )
    }

    private data class TestEnv(
        val trainingDao: FakeTrainingDao,
        val athletesRepository: FakeAthletesRepository,
        val weightDao: FakeAthleteWeightDao,
        val storage: FakeTrainingImportStorage,
        val repository: BackupRepository,
    )

    private fun piece(
        trainingId: Long,
        athleteId: Long,
        order: Int = 0,
        distanceMeters: Int = 2000,
        timeMillis: Long = 400_000L,
        strokeRate: Double = 30.0,
        avgHeartRate: Int? = null,
        maxHeartRate: Int? = null,
        recoveryHeartRate: Int? = null,
        lactate: Double? = null,
        sourceFileIndex: Int? = null,
        sourceIntervalIndex: Int? = null,
    ): TrainingAthletePieceEntity = TrainingAthletePieceEntity(
        trainingId = trainingId,
        athleteId = athleteId,
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = strokeRate,
        averageHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        recoveryHeartRate = recoveryHeartRate,
        lactate = lactate,
        sourceFileIndex = sourceFileIndex,
        sourceIntervalIndex = sourceIntervalIndex,
    )

    private fun backupPiece(
        athleteIndex: Int,
        order: Int = 0,
        distanceMeters: Int = 2000,
        timeMillis: Long = 400_000L,
        strokeRate: Double = 30.0,
        avgHeartRate: Int? = null,
        maxHeartRate: Int? = null,
        recoveryHeartRate: Int? = null,
        lactate: Double? = null,
        sourceFileIndex: Int? = null,
        sourceIntervalIndex: Int? = null,
    ): BackupPiece = BackupPiece(
        athleteIndex = athleteIndex,
        order = order,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
        strokeRate = strokeRate,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        recoveryHeartRate = recoveryHeartRate,
        lactate = lactate,
        sourceFileIndex = sourceFileIndex,
        sourceIntervalIndex = sourceIntervalIndex,
    )
}