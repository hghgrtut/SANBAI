package by.rowing.sanbaiteam.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
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

        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "rowing_database"

        val MIGRATIONS: Array<Migration> = arrayOf()
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object RowingDatabaseConstructor : RoomDatabaseConstructor<RowingDatabase> {
    override fun initialize(): RowingDatabase
}