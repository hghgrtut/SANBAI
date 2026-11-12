package by.rowing.sanbaiteam.core.presentation.compose

import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.concurrent.Volatile

@AllowDetektPublic
abstract class ComposeNavigator(
    replay: Int = 1,
    extraBufferCapacity: Int = 0,
) {

    private val _sharedFlow = MutableSharedFlow<NavigationEvent>(
        replay = replay,
        extraBufferCapacity = extraBufferCapacity,
    )

    val sharedFlow = _sharedFlow.asSharedFlow()

    /**
     * Сколько экранов сейчас слушают навигатор. Пока слушателей нет (Activity пересоздаётся),
     * события не копятся в replay-кэше: иначе накопленный `navigateBack` повторился бы после
     * восстановления UI и мог вытолкнуть последний экран — NavHost остался бы пустым (белый экран).
     */
    @Volatile
    private var attachedUiCount: Int = 0

    fun attachUi() {
        attachedUiCount++
    }

    fun detachUi() {
        attachedUiCount = (attachedUiCount - 1).coerceAtLeast(minimumValue = 0)
        if (attachedUiCount == 0) {
            resetCachedNavigation()
        }
    }

    /**
     * @param navTarget строковый путь навигации или сериализуемый объект
     * @param navOptions параметры навигации для [NavHostController]
     * @param additionalArgs dto-шка, которую нужно прокинуть на экран при навигации ([Parcelize])
     * @param removeCurrentScreen выставлять, если нужно удалить текущий экран из стека навигации
     * @param shouldRememberBottomSheet выставлять если есть необходимость после навигации обратно открывать повторно
     * боттом шит (при навигации куда-либо боттом шит принудительно удаляется из стека навигации)
     */
    fun navigateTo(
        navTarget: Any,
        navOptions: NavOptions?,
        additionalArgs: NavigationDataArgs? = null,
        removeCurrentScreen: Boolean = false,
        shouldRememberBottomSheet: Boolean = false,
    ) {
        emitEvent(
            NavigationEvent.NavigateForward(
                navTarget = navTarget,
                navOptions = navOptions,
                args = additionalArgs,
                removeCurrentScreen = removeCurrentScreen,
                shouldRememberBottomSheet = shouldRememberBottomSheet,
            )
        )
    }

    /**
     * @param navScreen экран, куда нужно сделать popUpTo, если null, то навигация будет по [navOptions],
     * заданных при навигации
     * @param additionalArgs результат работы экрана ([Parcelize])
     * @param restoreBottomSheetDialog выставлять если есть необходимость после навигации открыть повторно
     * боттом шит (если при навигации на экран был выставлен флаг shouldRememberBottomSheet)
     */
    fun navigateBack(
        navScreen: Any? = null,
        additionalArgs: NavigationDataArgs? = null,
        restoreBottomSheetDialog: Boolean = false,
    ) {
        emitEvent(
            NavigationEvent.NavigateBack(
                navTarget = navScreen,
                args = additionalArgs,
                restoreBottomSheetDialog = restoreBottomSheetDialog,
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun resetCachedNavigation() {
        _sharedFlow.resetReplayCache()
    }

    /** Полностью удалить стек навигации для текущего навигатора
     */
    fun clearBackStack() {
        emitEvent(NavigationEvent.ClearBackStack)
        resetCachedNavigation()
    }

    private fun emitEvent(event: NavigationEvent) {
        if (attachedUiCount > 0) {
            _sharedFlow.tryEmit(event)
        }
    }
}

@AllowDetektPublic
sealed interface NavigationEvent {

    data class NavigateForward(
        val navTarget: Any,
        val navOptions: NavOptions?,
        val args: NavigationDataArgs? = null,
        val removeCurrentScreen: Boolean = false,
        val shouldRememberBottomSheet: Boolean = false,
    ) : NavigationEvent

    data class NavigateBack(
        val navTarget: Any? = null,
        val args: NavigationDataArgs? = null,
        val restoreBottomSheetDialog: Boolean = false,
    ) : NavigationEvent

    data object ClearBackStack : NavigationEvent
}

@AllowDetektPublic
data class NavigationDataArgs(
    val key: String,
    val value: Any,
)