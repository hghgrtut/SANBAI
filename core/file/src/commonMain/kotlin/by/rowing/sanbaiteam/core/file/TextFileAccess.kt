package by.rowing.sanbaiteam.core.file

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Чтение и запись текстовых файлов по строковому дескриптору:
 * на Android — URI, на iOS — абсолютный путь.
 */
@AllowDetektPublic
interface TextFileAccess {

    suspend fun write(
        handle: String,
        text: String,
    ): Boolean

    suspend fun read(handle: String): String?
}