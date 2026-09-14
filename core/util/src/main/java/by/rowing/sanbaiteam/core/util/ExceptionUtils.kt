package by.rowing.sanbaiteam.core.util

internal object ExceptionUtils {
    fun <R> tryOrNull(body: () -> R): R? {
        return try {
            body()
        } catch (_: Throwable) {
            null
        }
    }
}
