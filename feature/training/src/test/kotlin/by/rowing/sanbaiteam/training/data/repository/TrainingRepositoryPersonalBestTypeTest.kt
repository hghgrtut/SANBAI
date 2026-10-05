package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.util.PersonalBestPercent
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

internal class TrainingRepositoryPersonalBestTypeTest {

    @Test
    fun `erg training percent uses erg record`() = runBlocking {
        val athletesRepository = FakeAthletesRepository(
            bests = listOf(
                best(
                    boatType = TrainingPieceType.SINGLE,
                    distanceMeters = 2000,
                    timeMillis = 420_000L
                ),
                best(
                    boatType = TrainingPieceType.CONCEPT_ROW_ERG,
                    distanceMeters = 2000,
                    timeMillis = 380_000L
                ),
            )
        )
        val repository = repository(TrainingPieceType.CONCEPT_ROW_ERG, athletesRepository)

        val state = repository.getTrainingDetail(TRAINING_ID)

        assertEquals("95.0%", state.pieces.single().percentOfPbFormatted)
        assertEquals(TrainingPieceType.CONCEPT_ROW_ERG, athletesRepository.requestedBoatType)
        assertEquals(
            PersonalBestPercent.DEFAULT_PB_DISTANCE_METERS,
            athletesRepository.requestedDistanceMeters,
        )
    }

    @Test
    fun `water training percent uses water record`() = runBlocking {
        val athletesRepository = FakeAthletesRepository(
            bests = listOf(
                best(
                    boatType = TrainingPieceType.CONCEPT_ROW_ERG,
                    distanceMeters = 2000,
                    timeMillis = 380_000L
                ),
                best(
                    boatType = TrainingPieceType.SINGLE,
                    distanceMeters = 2000,
                    timeMillis = 420_000L
                ),
            )
        )
        val repository = repository(TrainingPieceType.SINGLE, athletesRepository)

        val state = repository.getTrainingDetail(TRAINING_ID)

        assertEquals("105.0%", state.pieces.single().percentOfPbFormatted)
        assertEquals(TrainingPieceType.SINGLE, athletesRepository.requestedBoatType)
    }

    @Test
    fun `percent is dash when no record for training type`() = runBlocking {
        val athletesRepository = FakeAthletesRepository(
            bests = listOf(
                best(
                    boatType = TrainingPieceType.SINGLE,
                    distanceMeters = 2000,
                    timeMillis = 420_000L
                )
            )
        )
        val repository = repository(
            type = TrainingPieceType.CONCEPT_BIKE_ERG,
            athletesRepository = athletesRepository
        )

        val state = repository.getTrainingDetail(TRAINING_ID)

        assertEquals("—", state.pieces.single().percentOfPbFormatted)
    }

    @Test
    fun `percent is dash when record of training type has other distance`() = runBlocking {
        val athletesRepository = FakeAthletesRepository(
            bests = listOf(best(TrainingPieceType.CONCEPT_ROW_ERG, 5000, 1_200_000L))
        )
        val repository = repository(TrainingPieceType.CONCEPT_ROW_ERG, athletesRepository)

        val state = repository.getTrainingDetail(TRAINING_ID)

        assertEquals("—", state.pieces.single().percentOfPbFormatted)
        assertEquals(
            PersonalBestPercent.DEFAULT_PB_DISTANCE_METERS,
            athletesRepository.requestedDistanceMeters,
        )
    }

    private fun repository(
        type: TrainingPieceType,
        athletesRepository: FakeAthletesRepository,
    ) = TrainingRepository(
        dao = FakeTrainingDao(
            training = TrainingEntity(
                id = TRAINING_ID,
                dateMillis = DATE_MILLIS,
                type = type
            ),
            pieces = listOf(
                TrainingAthletePieceEntity(
                    trainingId = TRAINING_ID,
                    athleteId = ATHLETE_ID,
                    order = 0,
                    distanceMeters = 2000,
                    timeMillis = 400_000L,
                    strokeRate = 24.0,
                )
            ),
        ),
        athletesRepository = athletesRepository,
    )

    private fun best(
        boatType: TrainingPieceType,
        distanceMeters: Int,
        timeMillis: Long,
    ) = AthletePersonalBestEntity(
        athleteId = ATHLETE_ID,
        boatType = boatType,
        distanceMeters = distanceMeters,
        timeMillis = timeMillis,
    )

    private companion object {
        const val TRAINING_ID = 7L
        const val ATHLETE_ID = 3L
        const val DATE_MILLIS = 1_700_000_000_000L
    }

    private class FakeTrainingDao(
        private val training: TrainingEntity?,
        private val pieces: List<TrainingAthletePieceEntity>,
    ) : TrainingDao {

        override suspend fun insertTraining(training: TrainingEntity): Long = 1L

        override suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>) = Unit

        override suspend fun getAllTrainingEntities(): List<TrainingEntity> = listOfNotNull(training)

        override suspend fun getTrainingById(trainingId: Long): TrainingEntity? =
            training?.takeIf { it.id == trainingId }

        override suspend fun updateImportMetadata(
            trainingId: Long,
            relativePath: String?,
            deviceSerial: String?,
            sessionName: String?,
        ) = Unit

        override suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity> =
            pieces.filter { it.trainingId == trainingId }

        override suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long> =
            pieces.filter { it.trainingId == trainingId }.map { it.athleteId }.distinct()
    }

    private class FakeAthletesRepository(
        private val bests: List<AthletePersonalBestEntity>,
    ) : AthletesRepository {

        var requestedBoatType: TrainingPieceType? = null
            private set
        var requestedDistanceMeters: Int? = null
            private set

        override val allAthletes: Flow<List<AthleteItemModel>> = emptyFlow()

        override suspend fun getAllAthletesOnce(): List<AthleteEntity> = emptyList()

        override suspend fun addAthlete(athlete: AthleteEntity): Long = 1L

        override suspend fun getAthlete(athleteId: Long): AthleteEntity? = null

        override suspend fun updateAthlete(athlete: AthleteEntity) = Unit

        override suspend fun getPersonalBests(athleteId: Long): List<AthletePersonalBestEntity> =
            bests.filter { it.athleteId == athleteId }

        override suspend fun replacePersonalBests(
            athleteId: Long,
            bests: List<AthletePersonalBestEntity>,
        ) = Unit

        override suspend fun getPersonalBestsForAthletes(
            athleteIds: List<Long>,
            boatType: TrainingPieceType,
            distanceMeters: Int,
        ): Map<Long, AthletePersonalBestEntity> {
            requestedBoatType = boatType
            requestedDistanceMeters = distanceMeters
            return bests
                .filter {
                    it.athleteId in athleteIds &&
                            it.boatType == boatType &&
                            it.distanceMeters == distanceMeters
                }
                .associateBy { it.athleteId }
        }

        override suspend fun getAthleteNameMap(athleteIds: List<Long>): Map<Long, String> =
            athleteIds.associateWith { "Гребец $it" }

        override suspend fun findAthleteIdBySpeedCoachSerial(serial: String): Long? = null
    }
}