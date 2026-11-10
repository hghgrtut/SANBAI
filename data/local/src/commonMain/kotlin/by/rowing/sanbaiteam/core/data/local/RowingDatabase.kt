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
import by.rowing.sanbaiteam.training.data.entity.TrainingDetailHiddenFieldEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingEntity
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplaySettingEntity
import by.rowing.sanbaiteam.training.data.local.SavedTrainingFilterDao
import by.rowing.sanbaiteam.training.data.local.TrainingDao
import by.rowing.sanbaiteam.training.data.local.TrainingDetailHiddenFieldDao
import by.rowing.sanbaiteam.training.data.local.TrainingWorkDisplaySettingDao

@Database(
    entities = [
        AthleteEntity::class,
        AthletePersonalBestEntity::class,
        TrainingEntity::class,
        TrainingAthletePieceEntity::class,
        SavedTrainingFilterEntity::class,
        TrainingWorkDisplaySettingEntity::class,
        TrainingDetailHiddenFieldEntity::class,
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
    abstract fun trainingWorkDisplaySettingDao(): TrainingWorkDisplaySettingDao
    abstract fun trainingDetailHiddenFieldDao(): TrainingDetailHiddenFieldDao

    companion object {

        const val DATABASE_VERSION = 5
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${TrainingWorkDisplaySettingDao.TABLE_TRAINING_WORK_DISPLAY_SETTING}` (
                        `pieceType` TEXT NOT NULL,
                        `mode` TEXT NOT NULL,
                        PRIMARY KEY(`pieceType`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${TrainingDetailHiddenFieldDao.TABLE_TRAINING_DETAIL_HIDDEN_FIELD}` (
                        `field` TEXT NOT NULL,
                        PRIMARY KEY(`field`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object RowingDatabaseConstructor : RoomDatabaseConstructor<RowingDatabase> {
    override fun initialize(): RowingDatabase
}