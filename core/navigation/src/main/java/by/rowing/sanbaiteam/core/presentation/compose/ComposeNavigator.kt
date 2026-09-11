package by.rowing.sanbaiteam.core.presentation.compose

import android.os.Parcelable
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
        _sharedFlow.tryEmit(
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
        _sharedFlow.tryEmit(
            NavigationEvent.NavigateBack(
                navTarget = navScreen,
                args = additionalArgs,
                restoreBottomSheetDialog = restoreBottomSheetDialog,
            )
        )
    }

    fun resetCachedNavigation() {
        _sharedFlow.resetReplayCache()
    }

    /** Полностью удалить стек навигации для текущего навигатора
     */
    fun clearBackStack() {
        _sharedFlow.tryEmit(NavigationEvent.ClearBackStack)
        resetCachedNavigation()
    }
}

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

data class NavigationDataArgs(
    val key: String,
    val value: Any,
)

@Serializable
@Parcelize
data object NavigationNullObject : Parcelable