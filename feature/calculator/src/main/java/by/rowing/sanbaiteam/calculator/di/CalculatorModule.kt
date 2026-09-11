package by.rowing.sanbaiteam.calculator.di

import by.rowing.sanbaiteam.calculator.presentation.CalculatorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calculatorModule = module {
    viewModelOf(::CalculatorViewModel)
}