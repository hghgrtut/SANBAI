package by.rowing.sanbaiteam.training.data.trainingimport

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToURL

/**
 * Базовые файловые операции для iOS-слоя данных.
 * Работает с файлами в директории Documents приложения.
 */
@OptIn(ExperimentalForeignApi::class)
internal object IosTrainingFileManager {

    private val documentDirectory: NSURL by lazy {
        NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask,
        ).firstOrNull() as? NSURL
            ?: error("Documents directory not available")
    }

    fun resolvePath(relativePath: String): String {
        val cleaned = relativePath.trimStart('/')
        return documentDirectory.URLByAppendingPathComponent(cleaned)?.path
            ?: error("Cannot resolve path: $relativePath")
    }

    fun readText(relativePath: String): String? =
        NSData.dataWithContentsOfURL(url = NSURL.fileURLWithPath(path = resolvePath(relativePath)))
            ?.toByteArray()
            ?.decodeToString()

    fun deleteFile(relativePath: String) {
        NSFileManager.defaultManager.removeItemAtPath(
            path = resolvePath(relativePath),
            error = null
        )
    }

    /** Бросает [IllegalStateException], если запись не удалась (как IOException на Android). */
    fun writeText(
        relativePath: String,
        text: String
    ) {
        if (!writeFile(relativePath, text.encodeToByteArray().toNSData())) {
            error("Cannot write file: $relativePath")
        }
    }

    fun writeAbsoluteText(
        absolutePath: String,
        text: String
    ): Boolean = text.encodeToByteArray().toNSData().writeToURL(
        url = NSURL.fileURLWithPath(absolutePath),
        atomically = true
    )

    fun readAbsoluteText(absolutePath: String): String? =
        readAbsoluteBytes(absolutePath)?.decodeToString()

    fun readAbsoluteBytes(absolutePath: String): ByteArray? {
        val url = NSURL.fileURLWithPath(absolutePath)
        return NSData.dataWithContentsOfURL(url)?.toByteArray()
    }

    /** Текущее время в мс (wall clock, как System.currentTimeMillis() на Android). */
    fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970() * 1000.0).toLong()

    private fun writeFile(
        relativePath: String,
        data: NSData
    ): Boolean {
        NSFileManager.defaultManager.createDirectoryAtURL(
            url = NSURL.fileURLWithPath(
                resolvePath(relativePath.substringBeforeLast('/', ""))
            ),
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        val url = NSURL.fileURLWithPath(resolvePath(relativePath = relativePath))
        return data.writeToURL(url, atomically = true)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData.create(
        bytes = null,
        length = 0uL
    )
} else {
    usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(index = 0),
            length = size.toULong()
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isNotEmpty()) {
        // Явный получатель: без него usePinned привязался бы к NSData, а не к ByteArray.
        result.usePinned { pinned ->
            getBytes(
                buffer = pinned.addressOf(index = 0),
                length = length
            )
        }
    }
    return result
}
