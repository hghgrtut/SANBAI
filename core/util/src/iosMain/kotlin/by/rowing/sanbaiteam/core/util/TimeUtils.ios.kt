package by.rowing.sanbaiteam.core.util

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlin.time.Clock

@AllowDetektPublic
actual fun nowInMillis(): Long = Clock.System.now().toEpochMilliseconds()
