package by.rowing.sanbaiteam.training.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.TrainingDetailHiddenFieldEntity
import kotlinx.coroutines.flow.Flow

@AllowDetektPublic
@Dao
interface TrainingDetailHiddenFieldDao {

    @Query("SELECT field FROM $TABLE_TRAINING_DETAIL_HIDDEN_FIELD")
    fun observeHidden(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun hide(entity: TrainingDetailHiddenFieldEntity)

    @Query("DELETE FROM $TABLE_TRAINING_DETAIL_HIDDEN_FIELD WHERE field = :field")
    suspend fun show(field: String)

    @Query("DELETE FROM $TABLE_TRAINING_DETAIL_HIDDEN_FIELD")
    suspend fun clear()

    companion object {

        internal const val TABLE_TRAINING_DETAIL_HIDDEN_FIELD = "training_detail_hidden_field"
    }
}