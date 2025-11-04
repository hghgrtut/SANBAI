package by.rowing.sanbaiteam.core.presentation.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import kotlin.let

/**
 * Пункт назначения для compose-навигации
 * @param OutArg тип результата работы экрана, если он есть
 */
interface NavigationDestination<OutArg> {
    fun navigateTo(
        composeNavigator: ComposeNavigator,
        navOptions: NavOptions,
        shouldRememberBottomSheet: Boolean = false,
        removeCurrentScreen: Boolean = false,
    ) {
        composeNavigator.navigateTo(
            navTarget = this,
            navOptions = navOptions,
            shouldRememberBottomSheet = shouldRememberBottomSheet,
            removeCurrentScreen = removeCurrentScreen,
        )
    }

    fun navigateBack(
        navScreen: Any? = null,
        composeNavigator: ComposeNavigator,
        returnRes: OutArg? = null,
    ) {
        val additionalArg = if (returnRes != null) {
            NavigationDataArgs(
                key = this.getClassName(),
                value = returnRes
            )
        } else {
            null
        }
        composeNavigator.navigateBack(
            navScreen = navScreen,
            additionalArgs = additionalArg
        )
    }
}

inline fun <reified T : Any> getReturnToScreenNavOptions() =
    NavOptions.Builder()
        .setPopUpTo<T>(
            saveState = true,
            inclusive = false
        )
        .setLaunchSingleTop(true)
        .build()

@Composable
inline fun <reified OutArg> NavigationDestination<OutArg>.SubscribeResult(
    crossinline onResult: (OutArg) -> Unit,
) {
    val navHostController = LocalNavController.current
    navHostController.CollectArgResult<OutArg?>(
        argName = this.getClassName(),
        initialValue = null
    ) { res ->
        res?.let {
            onResult.invoke(res)
        }
    }
}

fun <T> NavBackStackEntry.getArgument(key: String): T? {
    val arg: T? = savedStateHandle.get<T>(key)
    if (arg != null) {
        savedStateHandle.remove<T>(key)
    }
    return arg
}