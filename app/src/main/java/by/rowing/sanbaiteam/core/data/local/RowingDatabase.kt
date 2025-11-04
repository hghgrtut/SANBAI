package by.rowing.sanbaiteam.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.training.data.entity.TrainingAthleteWork
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Database(
    entities = [AthleteEntity::class, TrainingPieceEntity::class, TrainingAthleteWork::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class RowingDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun trainingDao(): TrainingDao

    companion object {

        @Volatile
        private var INSTANCE: RowingDatabase? = null

        fun getInstance(context: Context): RowingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RowingDatabase::class.java,
                    "rowing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}