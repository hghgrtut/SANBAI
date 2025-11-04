package by.rowing.sanbaiteam.athlete.presentation.add

import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.presentation.add.compose.AddAthleteScreenActions
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal class AddAthleteViewModel(
    private val athleteRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator
) : ComposeBaseViewModel<AddAthleteState>(
    initialState = AddAthleteState(
        name = "",
        dateOfBirth = "",
        isMale = true,
        nameError = null,
        dateError = null
    )
), AddAthleteScreenActions {

    override fun onBackClick() {
        composeNavigator.navigateBack()
    }

    override fun onSaveClick() {
        if (validateForm()) {
            createAthleteFromState()?.let { athleteRepository.addAthlete(it) }
            onBackClick()
        } else {
            changeState {
                copy(
                    nameError = "Введите ФИО спортсмена".takeIf { name.isBlank() },
                    dateError = "Введите корректную дату".takeIf { !isValidDate(dateOfBirth) }
                )
            }
        }
    }

    override fun changeName(newName: String) {
        changeState {
            copy(
                name = newName,
                nameError = null
            )
        }
    }

    override fun changeBirthDate(newDate: String) {
        if (newDate.length <= DATE_PATTERN.length - DATE_PATTERN.count { it == '.' } && newDate.all { it.isDigit() }) {
            changeState {
                copy(
                    dateOfBirth = newDate,
                    dateError = null
                )
            }
        }
    }

    override fun changeGender(isMale: Boolean) {
        changeState { copy(isMale = isMale) }
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            format.isLenient = false
            format.parse(dateString) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateAge(dateString: String): Int {
        return try {
            val birthDate = getBirthDate(dateString) ?: return 0
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply { time = birthDate }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }

    private fun validateForm(): Boolean = state.name.isNotBlank() && isValidDate(state.dateOfBirth)

    private fun createAthleteFromState(): AthleteEntity? {
        return AthleteEntity(
            name = state.name,
            dateOfBirth = getBirthDate(state.dateOfBirth) ?: return null,
            isMale = state.isMale
        )
    }

    private fun getBirthDate(dateOfBirth: String): Date? =
        SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).parse(dateOfBirth)

    companion object {

        private const val DATE_PATTERN = "ddMMyyyy"
    }
}