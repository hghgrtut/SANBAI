package by.rowing.sanbaiteam.training.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface TrainingDao {

    data class TrainingListRawResult(
        val trainingId: Long,
        val trainingType: String,
        val workDescriptions: String,
        val maxPulseList: String,
        val pieceCount: Int
    )

    @Query("""
        SELECT 
            tp.trainingId as trainingId,
            tp.type as trainingType,
            GROUP_CONCAT(
                CASE 
                    WHEN tp.type = 'REST' THEN 'Отдых ' || (tp.duration / 60000) || 'мин'
                    WHEN tp.length IS NOT NULL THEN tp.length || 'м ' || (tp.duration / 60000) || 'мин'
                    ELSE (tp.duration / 60000) || 'мин'
                END
            ) as workDescriptions,
            GROUP_CONCAT(DISTINCT taw.maxPulse) as maxPulseList,
            COUNT(DISTINCT tp.id) as pieceCount
        FROM $TABLE_TRAINING_PIECE tp
        LEFT JOIN $TABLE_ATHLETE_WORK taw ON tp.id = taw.trainingPieceId
        WHERE tp.trainingId = :trainingId
        GROUP BY tp.trainingId, tp.type
    """)
    suspend fun getTrainingListRawResults(trainingId: Long): List<TrainingListRawResult>

    companion object {

        const val TABLE_TRAINING_PIECE = "table_training_piece"
        const val TABLE_ATHLETE_WORK = "table_athlete_work"
    }
}