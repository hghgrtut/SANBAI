package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.training.data.trainingimport.FakeTrainingImportStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TrainingRepositoryChartDataTest {

    @Test
    fun `piece with source mapping receives chart data`() = runTest {
        val storage = FakeTrainingImportStorage()
        storage.savedFiles[SOURCE_PATH] = RP3_CSV.encodeToByteArray()
        val repository = testTrainingRepository(
            trainings = listOf(training()),
            pieces = listOf(
                piece(
                    sourceFileIndex = 0,
                    sourceIntervalIndex = 0
                )
            ),
            importStorage = storage,
        )

        val detail = repository.getTrainingDetail(trainingId = 1L)

        val chartData = assertNotNull(detail.pieces.single().chartData)
        assertEquals(listOf(0.0, 15.0, 30.0), chartData.distance.map { it.value })
        assertEquals(90_000L, detail.pieces.single().timeMillis)
    }

    @Test
    fun `piece without source mapping has no chart data`() = runTest {
        val repository = testTrainingRepository(
            trainings = listOf(training()),
            pieces = listOf(
                piece(
                    sourceFileIndex = null,
                    sourceIntervalIndex = null
                )
            ),
        )

        val detail = repository.getTrainingDetail(trainingId = 1L)

        assertNull(detail.pieces.single().chartData)
    }

    private fun training(): TrainingEntity = TrainingEntity(
        id = 1L,
        dateMillis = 1L,
        type = TrainingPieceType.RP_3,
        sourceFileRelativePath = SOURCE_PATH,
    )

    private fun piece(
        sourceFileIndex: Int?,
        sourceIntervalIndex: Int?,
    ): TrainingAthletePieceEntity = TrainingAthletePieceEntity(
        trainingId = 1L,
        athleteId = 10L,
        order = 0,
        distanceMeters = 500,
        timeMillis = 90_000L,
        strokeRate = 20.0,
        sourceFileIndex = sourceFileIndex,
        sourceIntervalIndex = sourceIntervalIndex,
    )

    private companion object {
        const val SOURCE_PATH = "rp3.csv"
        val RP3_CSV = """
            id,workout_interval_id,stroke_number,stroke_rate,time,distance,estimated_500m_time,pulse
            ,1,1,20.0,1.0,10.0,120.0,100
            ,1,2,21.0,3.0,25.0,115.0,105
            ,1,3,22.0,5.0,40.0,110.0,0
        """.trimIndent()
    }
}