package by.rowing.sanbaiteam.core.presentation.screen.base

import androidx.lifecycle.ViewModel
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.core.presentation.screen.ComposeStateHolder
import by.rowing.sanbaiteam.core.presentation.screen.ComposeStateHolderImpl

@AllowDetektPublic
abstract class ComposeBaseViewModel<T>(initialState: T) : ViewModel(),
    ComposeStateHolder<T> by ComposeStateHolderImpl(initialState)