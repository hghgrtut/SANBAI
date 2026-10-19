package by.rowing.sanbaiteam.training.data.trainingimport

import by.rowing.sanbaiteam.training.data.models.FitLap
import by.rowing.sanbaiteam.training.data.models.FitRecord

internal class ByteReader(internal val bytes: ByteArray) {

    var position: Int = 0

    fun readByte(): Byte = bytes[position++]

    fun readUByte(): UByte = readByte().toUByte()

    fun readUShort(le: Boolean = true): UShort {
        val b0 = bytes[position++].toUByte().toUInt()
        val b1 = bytes[position++].toUByte().toUInt()
        return if (le) ((b1 shl 8) or b0).toUShort() else ((b0 shl 8) or b1).toUShort()
    }

    fun readUInt(le: Boolean = true): UInt {
        val b0 = bytes[position++].toUByte().toUInt()
        val b1 = bytes[position++].toUByte().toUInt()
        val b2 = bytes[position++].toUByte().toUInt()
        val b3 = bytes[position++].toUByte().toUInt()
        return if (le) {
            (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
        } else {
            (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
        }
    }

    fun readBytes(count: Int): ByteArray {
        val result = bytes.copyOfRange(position, position + count)
        position += count
        return result
    }
}

internal class FileHeader(
    val headerSize: Int,
    val dataSize: UInt,
    val isValid: Boolean
) {
    companion object {
        fun read(reader: ByteReader): FileHeader {
            val headerSize = reader.readUByte().toInt()
            reader.readUByte()
            reader.readUShort()
            val dataSize = reader.readUInt()
            val dataTypeBytes = reader.readBytes(4)
            val dataType = dataTypeBytes.decodeToString()
            if (headerSize == 14) {
                reader.readUShort()
            }
            val isValid = (headerSize == 12 || headerSize == 14) && dataType == FIT_DATA_TYPE
            return FileHeader(headerSize, dataSize, isValid)
        }
    }
}

internal class FieldDefinition(
    val num: UByte,
    val size: UByte,
    val baseType: UByte
)

internal class DevFieldDefinition(
    val num: UByte,
    val size: UByte
)

internal class MessageDefinition(
    val localMessageNumber: Int,
    val globalMessageNumber: UShort,
    val endianness: Int,
    val fieldDefinitions: List<FieldDefinition>,
    val devFieldDefinitions: List<DevFieldDefinition>
) {

    companion object {

        fun read(
            reader: ByteReader,
            headerByte: Byte
        ): MessageDefinition {
            reader.readByte()
            val architecture = reader.readUByte().toInt()
            val endianness = if (architecture == 0) 0 else 1
            val le = (endianness == 0)
            val global = reader.readUShort(le)
            val numFields = reader.readUByte().toInt()
            val fields = ArrayList<FieldDefinition>(numFields)
            for (i in 0 until numFields) {
                val num = reader.readUByte()
                val size = reader.readUByte()
                val baseType = reader.readUByte()
                fields.add(FieldDefinition(num, size, baseType))
            }
            val hasDev = (headerByte.toInt() and DEVELOPMENT_MASK) != 0
            val devFields = mutableListOf<DevFieldDefinition>()
            if (hasDev) {
                val numDevFields = reader.readUByte().toInt()
                for (i in 0 until numDevFields) {
                    val num = reader.readUByte()
                    val size = reader.readUByte()
                    reader.readUByte()
                    devFields.add(DevFieldDefinition(num, size))
                }
            }
            return MessageDefinition(
                localMessageNumber = (headerByte.toInt() and LOCAL_MESG_NUM_MASK),
                globalMessageNumber = global,
                endianness = endianness,
                fieldDefinitions = fields,
                devFieldDefinitions = devFields
            )
        }
    }
}

internal data class FitFileData(
    val isRowing: Boolean = false,
    val startTimeMillis: Long? = null,
    val laps: MutableList<FitLap> = mutableListOf(),
    val records: MutableList<FitRecord> = mutableListOf()
)

internal class FieldCollector {
    var timestamp: Long? = null
    var startTime: Long? = null
    var elapsed: Double? = null
    var distance: Double? = null
    var intensity: UByte? = null
    var heartRate: Int? = null
    var cadence: Int? = null
    var sport: UShort? = null
    var subSport: UShort? = null
    var fileIdTimeCreated: Long? = null
}

internal fun readDataSection(
    bytes: ByteArray,
    start: Int,
    end: Int
): FitFileData {
    var fileData = FitFileData()
    val localDefinitions = arrayOfNulls<MessageDefinition>(16)
    val reader = ByteReader(bytes)
    reader.position = start
    while (reader.position < end) {
        val headerInt = reader.bytes[reader.position].toInt() and 0xFF
        if ((headerInt and COMPRESSED_HEADER_MASK) != 0) {
            throw IllegalArgumentException("Compressed timestamp FIT files are not supported.")
        }
        if ((headerInt and DEFINITION_MASK) != 0) {
            val headerByte = reader.readByte()
            val def = MessageDefinition.read(reader, headerByte)
            val idx = def.localMessageNumber and 0xF
            if (idx >= 0 && idx < localDefinitions.size) {
                localDefinitions[idx] = def
            }
            continue
        }
        val headerByte = reader.readByte()
        val local = headerByte.toInt() and LOCAL_MESG_NUM_MASK
        val definition = if (local >= 0 && local < localDefinitions.size) localDefinitions[local] else null
        if (definition == null) {
            throw IllegalArgumentException("Missing FIT message definition.")
        }
        val collector = readDataMessage(
            reader = reader,
            definition = definition
        )
        when (definition.globalMessageNumber) {
            MESG_SESSION -> fileData = fileData.copy(
                isRowing = (collector.subSport == SUB_SPORT_INDOOR_ROWING || collector.sport == SPORT_ROWING),
                startTimeMillis = fileData.startTimeMillis ?: collector.startTime ?: collector.timestamp
            )
            MESG_LAP -> {
                val elapsedSec = collector.elapsed ?: 0.0
                if (elapsedSec > 0.0) {
                    val elapsedMs = (elapsedSec * 1000.0).toLong()
                    val startMs = collector.startTime ?: (collector.timestamp ?: fileData.startTimeMillis)
                    val endMs = collector.timestamp ?: (startMs?.plus(elapsedMs))
                    fileData.laps.add(
                        FitLap(
                            isActive = (collector.intensity == INTENSITY_ACTIVE),
                            startMillis = startMs ?: 0L,
                            endMillis = endMs ?: ((startMs ?: 0L) + elapsedMs),
                            elapsedMillis = elapsedMs,
                            distanceMeters = collector.distance ?: 0.0
                        )
                    )
                }
            }
            MESG_RECORD -> {
                val ts = collector.timestamp ?: continue
                val hr = collector.heartRate?.takeIf { it > 0 }
                val cad = collector.cadence?.takeIf { it > 0 }
                fileData.records.add(FitRecord(ts, hr, cad))
            }
        }
    }
    return fileData
}

internal fun readDataMessage(
    reader: ByteReader,
    definition: MessageDefinition
): FieldCollector {
    val collector = FieldCollector()
    val le = (definition.endianness == 0)
    for (fieldDefinition in definition.fieldDefinitions) {
        val size = fieldDefinition.size.toInt()
        val fieldStart = reader.position
        if (size > 0) {
            readFieldValue(
                reader = reader,
                collector = collector,
                globalMessageNumber = definition.globalMessageNumber,
                fieldNum = fieldDefinition.num,
                size = size,
                le = le
            )
        }
        // Поле всегда занимает ровно `size` байт. Если обработчик прочитал не столько
        // (номер поля совпал, а тип/размер — нет), выравниваем позицию, иначе разъедется
        // весь дальнейший поток сообщений.
        reader.position = fieldStart + size
    }
    for (dev in definition.devFieldDefinitions) {
        reader.position += dev.size.toInt()
    }
    return collector
}

private fun readFieldValue(
    reader: ByteReader,
    collector: FieldCollector,
    globalMessageNumber: UShort,
    fieldNum: UByte,
    size: Int,
    le: Boolean,
) {
    when (globalMessageNumber) {
        MESG_FILE_ID -> {
            if (fieldNum == FIELD_FILE_ID_TIME_CREATED && size == 4) {
                collector.fileIdTimeCreated = readTimestampOrNull(
                    reader = reader,
                    le = le
                )
            }
        }

        MESG_SESSION -> readSessionField(
            reader = reader,
            collector = collector,
            num = fieldNum,
            size = size,
            le = le
        )

        MESG_LAP -> readLapField(
            reader = reader,
            collector = collector,
            num = fieldNum,
            size = size,
            le = le
        )

        MESG_RECORD -> readRecordField(
            reader = reader,
            collector = collector,
            num = fieldNum,
            size = size,
            le = le
        )
        else -> Unit
    }
}

private fun scaleElapsed(raw: UInt): Double = raw.toDouble() / 1000.0

private fun scaleDistance(raw: UInt): Double = raw.toDouble() / 100.0

private fun readTimestampOrNull(
    reader: ByteReader,
    le: Boolean
): Long? {
    val raw = reader.readUInt(le)
    return if (raw == UInt.MAX_VALUE) {
        null
    } else {
        val seconds = raw.toLong() + FIT_EPOCH_OFFSET_SECONDS
        seconds * 1000L
    }
}

private fun readSessionField(
    reader: ByteReader,
    collector: FieldCollector,
    num: UByte,
    size: Int,
    le: Boolean
) {
    when (num) {
        FIELD_SESSION_TIMESTAMP -> if (size == 4) {
            readTimestampOrNull(reader, le)?.let { collector.timestamp = it }
        }
        FIELD_SESSION_START_TIME -> if (size == 4) {
            readTimestampOrNull(reader, le)?.let { collector.startTime = it }
        }
        FIELD_SESSION_SPORT -> if (size == 1) {
            collector.sport = reader.readUByte().toUShort()
        }
        FIELD_SESSION_SUB_SPORT -> if (size == 1) {
            collector.subSport = reader.readUByte().toUShort()
        }
        FIELD_SESSION_TOTAL_ELAPSED_TIME -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) collector.elapsed = scaleElapsed(raw)
        }
        FIELD_SESSION_TOTAL_DISTANCE -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) collector.distance = scaleDistance(raw)
        }
    }
}

private fun readLapField(
    reader: ByteReader,
    collector: FieldCollector,
    num: UByte,
    size: Int,
    le: Boolean
) {
    when (num) {
        FIELD_LAP_TIMESTAMP -> if (size == 4) {
            readTimestampOrNull(reader, le)?.let { collector.timestamp = it }
        }
        FIELD_LAP_START_TIME -> if (size == 4) {
            readTimestampOrNull(reader, le)?.let { collector.startTime = it }
        }
        FIELD_LAP_TOTAL_ELAPSED_TIME -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) collector.elapsed = scaleElapsed(raw)
        }
        FIELD_LAP_TOTAL_DISTANCE -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) collector.distance = scaleDistance(raw)
        }
        FIELD_LAP_INTENSITY -> if (size == 1) {
            collector.intensity = reader.readUByte()
        }
    }
}

private fun readRecordField(
    reader: ByteReader,
    collector: FieldCollector,
    num: UByte,
    size: Int,
    le: Boolean
) {
    when (num) {
        FIELD_RECORD_TIMESTAMP -> if (size == 4) {
            readTimestampOrNull(reader, le)?.let { collector.timestamp = it }
        }
        FIELD_RECORD_HEART_RATE -> if (size == 1) {
            val value = reader.readUByte().toInt()
            collector.heartRate = if (value == 0xFF) null else value
        }
        FIELD_RECORD_CADENCE -> if (size == 1) {
            val value = reader.readUByte().toInt()
            collector.cadence = if (value == 0xFF) null else value
        }
    }
}
