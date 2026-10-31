package by.rowing.sanbaiteam.training.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.SavedTrainingFilterEntity
import kotlinx.coroutines.flow.Flow

@AllowDetektPublic
@Dao
interface SavedTrainingFilterDao {

    @Query("SELECT * FROM $TABLE_SAVED_TRAINING_FILTER ORDER BY createdAtMillis ASC, id ASC")
    fun getAll(): Flow<List<SavedTrainingFilterEntity>>

    @Insert
    suspend fun insert(entity: SavedTrainingFilterEntity): Long

    @Query("UPDATE $TABLE_SAVED_TRAINING_FILTER SET payload = :payload WHERE id = :id")
    suspend fun updatePayload(
        id: Long,
        payload: String
    )

    @Query("UPDATE $TABLE_SAVED_TRAINING_FILTER SET name = :name WHERE id = :id")
    suspend fun rename(
        id: Long,
        name: String
    )

    @Query("DELETE FROM $TABLE_SAVED_TRAINING_FILTER WHERE id = :id")
    suspend fun delete(id: Long)

    companion object {
        const val TABLE_SAVED_TRAINING_FILTER = "saved_training_filter"
    }
}