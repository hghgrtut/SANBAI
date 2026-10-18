package by.rowing.sanbaiteam.athlete.presentation.add

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.presentation.add.compose.AddAthleteScreenActions
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_date_error
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_error
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.launch

internal class AddAthleteViewModel(
    private val athleteRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator
) : ComposeBaseViewModel<AddAthleteState>(
    initialState = AddAthleteState(
        name = "",
        dateOfBirth = "",
        speedCoachSerial = "",
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
            saveRower()
        } else {
            changeState {
                copy(
                    nameError = UiText.of(Res.string.athletes_add_athlete_name_error)
                        .takeIf { name.isBlank() },
                    dateError = UiText.of(Res.string.athletes_add_athlete_date_error)
                        .takeIf { !isValidDate(dateOfBirth) }
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
        if (newDate.length <= DATE_LENGTH && newDate.all { it.isDigit() }) {
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

    override fun changeSpeedCoachSerial(newSerial: String) {
        changeState {
            copy(speedCoachSerial = newSerial.filter { it.isLetterOrDigit() })
        }
    }

    private fun isValidDate(dateString: String): Boolean = TimeUtils.parseDateDigits(dateString) != null

    private fun validateForm(): Boolean = state.name.isNotBlank() && isValidDate(state.dateOfBirth)

    private fun saveRower() {
        viewModelScope.launch {
            athleteRepository.addAthlete(
                athlete = AthleteEntity(
                    name = state.name,
                    dateOfBirth = TimeUtils.parseDateDigits(state.dateOfBirth) ?: return@launch,
                    isMale = state.isMale,
                    speedCoachSerial = state.speedCoachSerial.trim().ifBlank { null }
                )
            )
            onBackClick()
        }
    }

    companion object {

        private const val DATE_LENGTH = 8
    }
}
