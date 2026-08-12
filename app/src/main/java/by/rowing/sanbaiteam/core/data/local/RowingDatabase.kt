package by.rowing.sanbaiteam.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
@TypeConverters(Converters::class)
internal abstract class RowingDatabase : RoomDatabase() {

    abstract fun athleteDao(): AthleteDao
    abstract fun athletePersonalBestDao(): AthletePersonalBestDao
    abstract fun trainingDao(): TrainingDao

    companion object {

        @Volatile
        private var INSTANCE: RowingDatabase? = null

        const val DATABASE_VERSION = 4

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    `index_athlete_personal_best_athleteId_boatType_distanceMeters`
                    ON `${AthletePersonalBestDao.TABLE_NAME}` (`athleteId`, `boatType`, `distanceMeters`)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_athlete_personal_best_athleteId`
                    ON `${AthletePersonalBestDao.TABLE_NAME}` (`athleteId`)
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RowingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RowingDatabase::class.java,
                    "rowing_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
