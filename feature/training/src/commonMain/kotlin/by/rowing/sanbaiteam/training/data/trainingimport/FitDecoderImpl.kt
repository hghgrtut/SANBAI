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
            val global = reader.readUShort(le = architecture == 0)
            val fields = List(size = reader.readUByte().toInt()) {
                FieldDefinition(
                    num = reader.readUByte(),
                    size = reader.readUByte(),
                    baseType = reader.readUByte()
                )
            }
            val devFields = if ((headerByte.toInt() and DEVELOPMENT_MASK) != 0) {
                List(size = reader.readUByte().toInt()) {
                    val num = reader.readUByte()
                    val size = reader.readUByte()
                    reader.readUByte()
                    DevFieldDefinition(
                        num = num,
                        size = size
                    )
                }
            } else {
                emptyList()
            }
            return MessageDefinition(
                localMessageNumber = (headerByte.toInt() and LOCAL_MESG_NUM_MASK),
                globalMessageNumber = global,
                endianness = if (architecture == 0) 0 else 1,
                fieldDefinitions = fields,
                devFieldDefinitions = devFields
            )
        }
    }
}

internal data class FitFileData(
    val isRowing: Boolean = false,
    val sport: UShort? = null,
    val subSport: UShort? = null,
    val manufacturer: UShort? = null,
    val product: UShort? = null,
    val softwareVersion: UShort? = null,
    val fileIdSerial: Long? = null,
    val deviceInfoSerial: Long? = null,
    val startTimeMillis: Long? = null,
    val laps: MutableList<FitLap> = mutableListOf(),
    val records: MutableList<FitRecord> = mutableListOf()
) {
    val deviceSerial: String?
        get() = (fileIdSerial ?: deviceInfoSerial)?.toString()
}

internal class FieldCollector {
    var timestamp: Long? = null
    var timestampSeconds: Long? = null
    var startTime: Long? = null
    var elapsed: Double? = null
    var distance: Double? = null
    var intensity: UByte? = null
    var heartRate: Int? = null
    var cadence: Int? = null
    var sport: UShort? = null
    var subSport: UShort? = null
    var manufacturer: UShort? = null
    var product: UShort? = null
    var softwareVersion: UShort? = null
    var fileIdSerial: Long? = null
    var deviceInfoSerial: Long? = null
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
    var lastTimestampSeconds: Long? = null

    while (reader.position < end) {
        val headerInt = reader.bytes[reader.position].toInt() and 0xFF
        if ((headerInt and COMPRESSED_HEADER_MASK) != 0) {
            val local = (headerInt shr 5) and 0x03
            val offset = (headerInt and 0x1F).toLong()
            reader.readByte()
            val definition: MessageDefinition = localDefinitions[local] ?: throwFitMessageDefinitionException()
            val compressedSeconds = nextCompressedTimestamp(
                last = lastTimestampSeconds,
                offset = offset
            )
            lastTimestampSeconds = compressedSeconds
            val collector = readDataMessage(
                reader = reader,
                definition = definition,
                compressedTimestampSeconds = compressedSeconds
            )
            fileData = applyCollector(
                fileData = fileData,
                definition = definition,
                collector = collector
            )
            continue
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
            throwFitMessageDefinitionException()
        }
        val collector = readDataMessage(
            reader = reader,
            definition = definition
        )
        collector.timestampSeconds?.let { lastTimestampSeconds = it }
        fileData = applyCollector(fileData, definition, collector)
    }
    return fileData
}

private fun nextCompressedTimestamp(
    last: Long?,
    offset: Long
): Long {
    if (last == null) return offset
    var timestamp = (last and COMPRESSED_TIMESTAMP_MASK) or offset
    if (timestamp < last) timestamp += COMPRESSED_TIMESTAMP_ROLLOVER
    return timestamp
}

private fun applyCollector(
    fileData: FitFileData,
    definition: MessageDefinition,
    collector: FieldCollector
): FitFileData = when (definition.globalMessageNumber) {
    MESG_FILE_ID -> fileData.copy(
        manufacturer = collector.manufacturer ?: fileData.manufacturer,
        product = collector.product ?: fileData.product,
        fileIdSerial = collector.fileIdSerial ?: fileData.fileIdSerial,
    )

    MESG_DEVICE_INFO -> fileData.copy(
        deviceInfoSerial = collector.deviceInfoSerial ?: fileData.deviceInfoSerial,
        softwareVersion = collector.softwareVersion ?: fileData.softwareVersion,
    )

    MESG_SESSION -> fileData.copy(
        isRowing = (collector.subSport == SUB_SPORT_INDOOR_ROWING || collector.sport == SPORT_ROWING),
        sport = collector.sport,
        subSport = collector.subSport,
        startTimeMillis = fileData.startTimeMillis ?: collector.startTime ?: collector.timestamp
    )

    MESG_LAP -> fileData.appendLap(collector)

    MESG_RECORD -> {
        fileData.also { fileData ->
            fileData.records.add(
                FitRecord(
                    timestampMillis = collector.timestamp ?: return fileData,
                    heartRate = collector.heartRate?.takeIf { it > 0 },
                    cadence = collector.cadence?.takeIf { it > 0 }
                )
            )
        }
    }

    else -> fileData
}

private fun FitFileData.appendLap(collector: FieldCollector): FitFileData {
    val elapsedSec = collector.elapsed ?: 0.0
    if (elapsedSec <= 0.0) return this
    val elapsedMs = (elapsedSec * 1000.0).toLong()
    val startMs = collector.startTime ?: (collector.timestamp ?: startTimeMillis)
    val endMs = collector.timestamp ?: (startMs?.plus(elapsedMs))
    laps.add(
        FitLap(
            isActive = (collector.intensity == INTENSITY_ACTIVE),
            startMillis = startMs ?: 0L,
            endMillis = endMs ?: ((startMs ?: 0L) + elapsedMs),
            elapsedMillis = elapsedMs,
            distanceMeters = collector.distance ?: 0.0
        )
    )
    return this
}

internal fun readDataMessage(
    reader: ByteReader,
    definition: MessageDefinition,
    compressedTimestampSeconds: Long? = null
): FieldCollector {
    val collector = FieldCollector()
    if (compressedTimestampSeconds != null) {
        collector.timestampSeconds = compressedTimestampSeconds
        collector.timestamp = fitSecondsToMillis(compressedTimestampSeconds)
    }
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
        // Поле всегда занимает ровно `size` байт. Если обработчик прочитал меньше (номер поля совпал,
        // а тип/размер — нет), выравниваем позицию, иначе разъедется весь дальнейший поток сообщений.
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
        MESG_FILE_ID -> readFileIdField(
            reader = reader,
            collector = collector,
            num = fieldNum,
            size = size,
            le = le
        )

        MESG_DEVICE_INFO -> readDeviceInfoField(
            reader = reader,
            collector = collector,
            num = fieldNum,
            size = size,
            le = le
        )

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

private fun fitSecondsToMillis(seconds: Long): Long = (seconds + FIT_EPOCH_OFFSET_SECONDS) * 1000L

private fun throwFitMessageDefinitionException(): Nothing =
    throw IllegalArgumentException("Missing FIT message definition.")

private fun readTimestampSecondsOrNull(
    reader: ByteReader,
    le: Boolean
): Long? {
    val raw = reader.readUInt(le)
    return if (raw == UInt.MAX_VALUE) null else raw.toLong()
}

private fun readFileIdField(
    reader: ByteReader,
    collector: FieldCollector,
    num: UByte,
    size: Int,
    le: Boolean
) {
    when (num) {
        FIELD_FILE_ID_MANUFACTURER -> if (size == 2) {
            collector.manufacturer = reader.readUShort(le)
        }

        FIELD_FILE_ID_PRODUCT -> if (size == 2) {
            collector.product = reader.readUShort(le)
        }

        FIELD_FILE_ID_SERIAL -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != 0u && raw != UInt.MAX_VALUE) {
                collector.fileIdSerial = raw.toLong()
            }
        }

        FIELD_FILE_ID_TIME_CREATED -> if (size == 4) {
            collector.fileIdTimeCreated = readTimestampSecondsOrNull(reader, le)
                ?.let(::fitSecondsToMillis)
        }
    }
}

private fun readDeviceInfoField(
    reader: ByteReader,
    collector: FieldCollector,
    num: UByte,
    size: Int,
    le: Boolean
) {
    when (num) {
        FIELD_DEVICE_INFO_PRODUCT -> if (size == 2) {
            collector.product = reader.readUShort(le)
        }

        FIELD_DEVICE_INFO_SERIAL -> when (size) {
            2 -> {
                val raw = reader.readUShort(le).toInt()
                if (raw != 0 && raw != 0xFFFF) collector.deviceInfoSerial = raw.toLong()
            }

            4 -> {
                val raw = reader.readUInt(le)
                if (raw != 0u && raw != UInt.MAX_VALUE) collector.deviceInfoSerial = raw.toLong()
            }
        }

        FIELD_DEVICE_INFO_SOFTWARE_VERSION -> if (size == 2) {
            collector.softwareVersion = reader.readUShort(le)
        }
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
            readTimestampSecondsOrNull(reader, le)?.let { seconds ->
                collector.timestampSeconds = seconds
                collector.timestamp = fitSecondsToMillis(seconds)
            }
        }

        FIELD_SESSION_START_TIME -> if (size == 4) {
            readTimestampSecondsOrNull(reader, le)?.let { collector.startTime = fitSecondsToMillis(it) }
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
            readTimestampSecondsOrNull(reader, le)?.let { seconds ->
                collector.timestampSeconds = seconds
                collector.timestamp = fitSecondsToMillis(seconds)
            }
        }

        FIELD_LAP_START_TIME -> if (size == 4) {
            readTimestampSecondsOrNull(reader, le)?.let { collector.startTime = fitSecondsToMillis(it) }
        }

        FIELD_LAP_TOTAL_ELAPSED_TIME -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) collector.elapsed = scaleElapsed(raw)
        }

        FIELD_LAP_TOTAL_DISTANCE -> if (size == 4) {
            val raw = reader.readUInt(le)
            if (raw != UInt.MAX_VALUE) {
                collector.distance = scaleDistance(raw)
            }
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
            readTimestampSecondsOrNull(
                reader = reader,
                le = le
            )?.let { seconds ->
                collector.timestampSeconds = seconds
                collector.timestamp = fitSecondsToMillis(seconds)
            }
        }

        FIELD_RECORD_HEART_RATE -> if (size == 1) {
            collector.heartRate = reader.readUByte().toInt().takeIf { it != 0xFF }
        }

        FIELD_RECORD_CADENCE -> if (size == 1) {
            collector.cadence = reader.readUByte().toInt().takeIf { it != 0xFF }
        }
    }
}
