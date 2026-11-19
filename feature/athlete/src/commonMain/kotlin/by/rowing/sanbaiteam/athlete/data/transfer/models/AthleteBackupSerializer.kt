package by.rowing.sanbaiteam.athlete.data.transfer.models

import kotlinx.serialization.json.Json

internal object AthleteBackupSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun encode(backup: AthleteBackup): String = json.encodeToString(backup)

    fun decode(text: String): AthleteBackup = json.decodeFromString(text)
}