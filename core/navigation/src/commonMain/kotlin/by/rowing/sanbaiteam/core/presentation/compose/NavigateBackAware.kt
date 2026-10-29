package by.rowing.sanbaiteam.core.presentation.compose

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Экран, который сам обрабатывает системное «Назад».
 * Подключается автоматически в [composable] из этого пакета.
 */
@AllowDetektPublic
interface NavigateBackAware {

    fun navigateBack()

    companion object {

        fun empty(): NavigateBackAware = object : NavigateBackAware {

            override fun navigateBack() = Unit
        }
    }
}