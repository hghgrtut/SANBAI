package by.rowing.sanbaiteam.backup.data.serializer

import by.rowing.sanbaiteam.backup.data.model.SanbaiBackup
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@AllowDetektPublic
class BackupSerializer(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
) {

    fun encode(backup: SanbaiBackup): String = json.encodeToString(backup)

    fun decode(text: String): SanbaiBackup {
        val element = runCatching { json.parseToJsonElement(text) }.getOrElse {
            throw UnsupportedBackupFormatException("Backup is not a valid JSON")
        }
        val root = element as? JsonObject ?: throw UnsupportedBackupFormatException("Backup root must be a JSON object")

        val kind = root[KIND_FIELD]?.jsonPrimitive?.contentOrNull
        val formatVersion = root[FORMAT_VERSION_FIELD]?.jsonPrimitive?.intOrNull
        if (kind != SanbaiBackup.KIND || formatVersion != SanbaiBackup.FORMAT_VERSION) {
            throw UnsupportedBackupFormatException(
                "Unsupported backup format: kind=$kind, formatVersion=$formatVersion"
            )
        }

        return runCatching { json.decodeFromJsonElement(SanbaiBackup.serializer(), root) }
            .getOrElse { throw UnsupportedBackupFormatException("Backup content is invalid") }
    }

    companion object {

        private const val KIND_FIELD = "kind"
        private const val FORMAT_VERSION_FIELD = "formatVersion"
    }
}

@AllowDetektPublic
class UnsupportedBackupFormatException(message: String) : IllegalArgumentException(message)
