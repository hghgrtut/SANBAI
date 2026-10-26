package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.trainingimport.TrainingImportContainer.EXTENSION

/**
 * Binary container that keeps the original `.fit` (or `.csv`) files of a multi-source import.
 *
 * A single imported file is stored as-is, without wrapping. Several files are packed into
 * [EXTENSION] so that the exact original bytes survive and can be extracted back later.
 *
 * Layout (little-endian):
 * `"SANBAIFIT"` magic, `u8` version, `u16` file count, then for every file `u32` length + bytes.
 */
internal object TrainingImportContainer {

    const val EXTENSION = "fitpack"

    private val MAGIC = "SANBAIFIT".encodeToByteArray()
    private const val VERSION = 1
    private const val HEADER_SIZE = 9 + 1 + 2
    private const val LENGTH_SIZE = 4

    fun isContainer(bytes: ByteArray): Boolean =
        bytes.size >= HEADER_SIZE && MAGIC.indices.all { bytes[it] == MAGIC[it] }

    fun pack(files: List<ByteArray>): ByteArray {
        require(files.isNotEmpty()) { "At least one file is required." }
        if (files.size == 1) return files.first()

        val result = ByteArray(size = HEADER_SIZE + files.sumOf { LENGTH_SIZE + it.size })
        var position = 0
        MAGIC.copyInto(
            destination = result,
            destinationOffset = position
        )
        position += MAGIC.size
        result[position++] = VERSION.toByte()
        position = writeUShort(
            target = result,
            position = position,
            value = files.size
        )
        files.forEach { file ->
            position = writeUInt(result, position, file.size)
            file.copyInto(result, position)
            position += file.size
        }
        return result
    }

    fun unpack(bytes: ByteArray): List<ByteArray>? {
        if (!isContainer(bytes)) return null
        var position = MAGIC.size
        val version = bytes[position++].toInt() and 0xFF
        if (version != VERSION) return null
        val count = readUShort(bytes, position)
        position += 2
        val files = ArrayList<ByteArray>(count)
        repeat(count) {
            if (position + LENGTH_SIZE > bytes.size) return null
            val length = readUInt(bytes, position)
            position += LENGTH_SIZE
            if (length < 0 || position + length > bytes.size) return null
            files.add(bytes.copyOfRange(position, position + length))
            position += length
        }
        return files.takeIf { position == bytes.size }
    }

    fun unpackOrSingle(bytes: ByteArray): List<ByteArray> = unpack(bytes) ?: listOf(bytes)

    private fun writeUShort(
        target: ByteArray,
        position: Int,
        value: Int
    ): Int {
        target[position] = (value and 0xFF).toByte()
        target[position + 1] = ((value shr 8) and 0xFF).toByte()
        return position + 2
    }

    private fun writeUInt(
        target: ByteArray,
        position: Int,
        value: Int
    ): Int {
        target[position] = (value and 0xFF).toByte()
        target[position + 1] = ((value shr 8) and 0xFF).toByte()
        target[position + 2] = ((value shr 16) and 0xFF).toByte()
        target[position + 3] = ((value shr 24) and 0xFF).toByte()
        return position + 4
    }

    private fun readUShort(
        source: ByteArray,
        position: Int
    ): Int = (source[position].toInt() and 0xFF) or
            ((source[position + 1].toInt() and 0xFF) shl 8)

    private fun readUInt(
        source: ByteArray,
        position: Int
    ): Int = (source[position].toInt() and 0xFF) or
            ((source[position + 1].toInt() and 0xFF) shl 8) or
            ((source[position + 2].toInt() and 0xFF) shl 16) or
            ((source[position + 3].toInt() and 0xFF) shl 24)
}