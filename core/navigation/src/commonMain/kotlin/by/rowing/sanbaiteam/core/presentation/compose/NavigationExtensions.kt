package by.rowing.sanbaiteam.core.presentation.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.component.BackHandler
import androidx.navigation.compose.composable as androidxComposable

/**
 * Аналог [androidxComposable], который автоматически подключает обработку
 * системного «Назад» для вьюмоделей, реализующих [NavigateBackAware].
 */
@AllowDetektPublic
inline fun <reified T : Any, VM : Any> NavGraphBuilder.composable(
    noinline viewModelFactory: @Composable (T) -> VM,
    noinline content: @Composable (VM) -> Unit,
) {
    androidxComposable<T> { backStackEntry ->
        val route = backStackEntry.toRoute<T>()
        val viewModel = viewModelFactory(route)
        if (viewModel is NavigateBackAware) {
            BackHandler {
                viewModel.navigateBack()
            }
        }
        content(viewModel)
    }
}