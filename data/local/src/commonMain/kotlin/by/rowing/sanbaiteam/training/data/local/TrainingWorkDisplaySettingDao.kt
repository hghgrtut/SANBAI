package by.rowing.sanbaiteam.training.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplaySettingEntity
import kotlinx.coroutines.flow.Flow

@AllowDetektPublic
@Dao
interface TrainingWorkDisplaySettingDao {

    @Query("SELECT * FROM $TABLE_TRAINING_WORK_DISPLAY_SETTING")
    fun observeAll(): Flow<List<TrainingWorkDisplaySettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrainingWorkDisplaySettingEntity)

    companion object {

        internal const val TABLE_TRAINING_WORK_DISPLAY_SETTING = "training_work_display_setting"
    }
}
