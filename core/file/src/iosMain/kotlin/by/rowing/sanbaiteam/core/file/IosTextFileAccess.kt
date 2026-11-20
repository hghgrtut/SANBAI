package by.rowing.sanbaiteam.core.file

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.Foundation.writeToURL

internal class IosTextFileAccess : TextFileAccess {

    override suspend fun write(
        handle: String,
        text: String,
    ): Boolean = runCatching {
        text.encodeToByteArray().toNSData().writeToURL(
            url = NSURL.fileURLWithPath(handle),
            atomically = true,
        )
    }.getOrDefault(false)

    override suspend fun read(handle: String): String? = runCatching {
        NSData.dataWithContentsOfURL(url = NSURL.fileURLWithPath(handle))?.toByteArray()?.decodeToString()
    }.getOrNull()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData.create(
        bytes = null,
        length = 0uL,
    )
} else {
    usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(index = 0),
            length = size.toULong(),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isNotEmpty()) {
        result.usePinned { pinned ->
            getBytes(
                buffer = pinned.addressOf(index = 0),
                length = length,
            )
        }
    }
    return result
}