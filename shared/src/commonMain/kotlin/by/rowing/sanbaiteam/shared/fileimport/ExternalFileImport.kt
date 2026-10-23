package by.rowing.sanbaiteam.shared.fileimport

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Внешний запрос на открытие файла тренировки.
 *
 * Android передаёт URI напрямую в [by.rowing.sanbaiteam.shared.App], а iOS — через
 * этот мост из Swift (см. `onOpenURL`), потому что файл может прийти как при холодном
 * старте, так и во время работы приложения.
 */
@AllowDetektPublic
object ExternalFileImport {

    private val pendingUriState = MutableStateFlow<String?>(null)

    val pendingUri: StateFlow<String?> = pendingUriState.asStateFlow()

    fun submit(uri: String) {
        if (uri.isNotBlank()) {
            pendingUriState.value = uri
        }
    }

    fun consume(uri: String) {
        pendingUriState.compareAndSet(
            expect = uri,
            update = null
        )
    }
}
