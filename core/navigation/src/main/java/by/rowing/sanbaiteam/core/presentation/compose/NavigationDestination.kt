package by.rowing.sanbaiteam.core.presentation.compose

import androidx.navigation.NavOptions

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

inline fun <reified T : Any> getReturnToScreenNavOptions() = NavOptions.Builder()
    .setPopUpTo<T>(
        saveState = true,
        inclusive = false
    )
    .setLaunchSingleTop(true)
    .build()