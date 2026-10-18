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

        const val DATABASE_VERSION = 6
        const val DATABASE_NAME = "rowing_database"

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${AthletePersonalBestDao.TABLE_NAME}` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `athleteId` INTEGER NOT NULL,
                        `boatType` TEXT NOT NULL,
                        `distanceMeters` INTEGER NOT NULL,
                        `timeMillis` INTEGER NOT NULL,
                        FOREIGN KEY(`athleteId`) REFERENCES `${AthleteDao.TABLE_NAME}`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                connection.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_athlete_personal_best_athleteId_boatType_distanceMeters`
                    ON `${AthletePersonalBestDao.TABLE_NAME}` (`athleteId`, `boatType`, `distanceMeters`)
                    """.trimIndent()
                )
                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_athlete_personal_best_athleteId`
                    ON `${AthletePersonalBestDao.TABLE_NAME}` (`athleteId`)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                for (column in listOf("avgHeartRate", "maxHeartRate", "recoveryHeartRate")) {
                    connection.execSQL("ALTER TABLE `${TrainingDao.TABLE_TRAINING_ATHLETE_PIECE}` ADD COLUMN `$column` INTEGER")
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `${TrainingDao.TABLE_TRAINING_ATHLETE_PIECE}` ADD COLUMN `lactate` REAL")
            }
        }

        val MIGRATIONS = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object RowingDatabaseConstructor : RoomDatabaseConstructor<RowingDatabase> {
    override fun initialize(): RowingDatabase
}
