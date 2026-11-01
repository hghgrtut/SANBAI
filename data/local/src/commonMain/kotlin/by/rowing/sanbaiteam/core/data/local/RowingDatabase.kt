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
import by.rowing.sanbaiteam.training.data.entity.SavedTrainingFilterEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingAthletePieceEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao
import by.rowing.sanbaiteam.training.data.local.TrainingDao

@Database(
    entities = [
        AthleteEntity::class,
        AthletePersonalBestEntity::class,
        TrainingEntity::class,
        TrainingAthletePieceEntity::class,
        SavedTrainingFilterEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@ConstructedBy(RowingDatabaseConstructor::class)
internal abstract class RowingDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun athletePersonalBestDao(): AthletePersonalBestDao
    abstract fun trainingDao(): TrainingDao
    abstract fun savedTrainingFilterDao(): SavedTrainingFilterDao

    companion object {

        const val DATABASE_VERSION = 3
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${SavedTrainingFilterDao.TABLE_SAVED_TRAINING_FILTER}` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `payload` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object RowingDatabaseConstructor : RoomDatabaseConstructor<RowingDatabase> {
    override fun initialize(): RowingDatabase
}