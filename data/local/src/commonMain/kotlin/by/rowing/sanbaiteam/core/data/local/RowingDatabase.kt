package by.rowing.sanbaiteam.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.local.AthleteDao
import by.rowing.sanbaiteam.athlete.data.local.AthletePersonalBestDao
import by.rowing.sanbaiteam.core.data.local.RowingDatabase.Companion.DATABASE_VERSION
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Database(
    entities = [
        AthleteEntity::class,
        AthletePersonalBestEntity::class,
        TrainingEntity::class,
        TrainingAthletePieceEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@ConstructedBy(RowingDatabaseConstructor::class)
internal abstract class RowingDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun athletePersonalBestDao(): AthletePersonalBestDao
    abstract fun trainingDao(): TrainingDao

    companion object {

        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "rowing_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE `${TrainingDao.TABLE_TRAINING_ATHLETE_PIECE}` ADD COLUMN `sourceFileIndex` INTEGER"
                )
                connection.execSQL(
                    "ALTER TABLE `${TrainingDao.TABLE_TRAINING_ATHLETE_PIECE}` ADD COLUMN `sourceIntervalIndex` INTEGER"
                )
            }
        }

        val MIGRATIONS = arrayOf(MIGRATION_1_2)
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object RowingDatabaseConstructor : RoomDatabaseConstructor<RowingDatabase> {
    override fun initialize(): RowingDatabase
}