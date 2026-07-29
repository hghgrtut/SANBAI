package by.rowing.sanbaiteam.training.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity

@Dao
internal interface TrainingDao {

    @Insert
    suspend fun insertTraining(training: TrainingEntity): Long

    @Insert
    suspend fun insertPieces(pieces: List<TrainingAthletePieceEntity>)

    @Transaction
    suspend fun saveTrainingWithPieces(
        training: TrainingEntity,
        pieces: List<TrainingAthletePieceEntity>
    ): Long {
        val trainingId = insertTraining(training)
        insertPieces(pieces.map { it.copy(trainingId = trainingId) })
        return trainingId
    }

    @Query("SELECT * FROM $TABLE_TRAINING ORDER BY dateMillis DESC")
    suspend fun getAllTrainingEntities(): List<TrainingEntity>

    @Query("SELECT * FROM $TABLE_TRAINING WHERE id = :trainingId")
    suspend fun getTrainingById(trainingId: Long): TrainingEntity?

    @Query(
        """
        UPDATE $TABLE_TRAINING
        SET sourceCsvRelativePath = :relativePath,
            sourceDeviceSerial = :deviceSerial,
            sourceSessionName = :sessionName
        WHERE id = :trainingId
        """
    )
    suspend fun updateImportMetadata(
        trainingId: Long,
        relativePath: String?,
        deviceSerial: String?,
        sessionName: String?,
    )

    @Query(
        """
        SELECT * FROM $TABLE_TRAINING_ATHLETE_PIECE
        WHERE trainingId = :trainingId
        ORDER BY athleteId, `order`
        """
    )
    suspend fun getPiecesForTraining(trainingId: Long): List<TrainingAthletePieceEntity>

    @Query(
        """
        SELECT DISTINCT athleteId FROM $TABLE_TRAINING_ATHLETE_PIECE
        WHERE trainingId = :trainingId
        """
    )
    suspend fun getAthleteIdsInTraining(trainingId: Long): List<Long>

    companion object {
        const val TABLE_TRAINING = "table_training"
        const val TABLE_TRAINING_ATHLETE_PIECE = "table_training_athlete_piece"
    }
}
