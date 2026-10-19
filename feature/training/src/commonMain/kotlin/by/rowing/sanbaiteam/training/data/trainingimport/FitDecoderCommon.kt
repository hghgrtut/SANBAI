package by.rowing.sanbaiteam.training.data.trainingimport

internal const val FIT_HEADER_MIN_SIZE = 12
internal const val FIT_HEADER_MAGIC_OFFSET = 8
internal const val FIT_DATA_TYPE = ".FIT"
internal const val CRC_SIZE = 2

internal const val COMPRESSED_HEADER_MASK = 0x80
internal const val DEFINITION_MASK = 0x40
internal const val DEVELOPMENT_MASK = 0x20
internal const val LOCAL_MESG_NUM_MASK = 0x0F

internal val FIELD_FILE_ID_TIME_CREATED = 4.toUByte()
internal val FIELD_SESSION_TIMESTAMP = 253.toUByte()
internal val FIELD_SESSION_START_TIME = 2.toUByte()
internal val FIELD_SESSION_SPORT = 5.toUByte()
internal val FIELD_SESSION_SUB_SPORT = 6.toUByte()
internal val FIELD_SESSION_TOTAL_ELAPSED_TIME = 7.toUByte()
internal val FIELD_SESSION_TOTAL_DISTANCE = 9.toUByte()
internal val FIELD_LAP_TIMESTAMP = 253.toUByte()
internal val FIELD_LAP_START_TIME = 2.toUByte()
internal val FIELD_LAP_TOTAL_ELAPSED_TIME = 7.toUByte()
internal val FIELD_LAP_TOTAL_DISTANCE = 9.toUByte()
internal val FIELD_LAP_INTENSITY = 23.toUByte()
internal val FIELD_RECORD_TIMESTAMP = 253.toUByte()
internal val FIELD_RECORD_HEART_RATE = 3.toUByte()
internal val FIELD_RECORD_CADENCE = 4.toUByte()

internal val MESG_FILE_ID = 0.toUShort()
internal val MESG_SESSION = 18.toUShort()
internal val MESG_LAP = 19.toUShort()
internal val MESG_RECORD = 20.toUShort()

internal val SPORT_FITNESS_EQUIPMENT = 4.toUShort()
internal val SPORT_ROWING = 15.toUShort()
internal val SPORT_OTHER = 0.toUShort()

internal val SUB_SPORT_INDOOR_ROWING = 14.toUShort()
internal val SUB_SPORT_GENERIC = 0.toUShort()

internal val INTENSITY_ACTIVE = 0.toUByte()
internal val INTENSITY_REST = 1.toUByte()

internal const val FIT_EPOCH_OFFSET_SECONDS: Long = 631065600L