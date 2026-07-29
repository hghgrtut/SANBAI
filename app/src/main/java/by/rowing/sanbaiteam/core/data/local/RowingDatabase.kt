package by.rowing.sanbaiteam.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.core.data.local.RowingDatabase.Companion.DATABASE_VERSION
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Database(
    entities = [
        AthleteEntity::class,
        TrainingEntity::class,
        TrainingAthletePieceEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class RowingDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun trainingDao(): TrainingDao

    companion object {

        @Volatile
        private var INSTANCE: RowingDatabase? = null

        private const val DATABASE_VERSION = 3

        fun getInstance(context: Context): RowingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RowingDatabase::class.java,
                    "rowing_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
