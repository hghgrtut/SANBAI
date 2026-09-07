package by.rowing.sanbaiteam.athlete.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.AndroidResourceUtils
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class AthleteDetailViewModel(
    private val resourceUtils: AndroidResourceUtils,
    private val athleteRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator,
    private val athleteId: Long,
) : ComposeBaseViewModel<AthleteDetailState>(
    initialState = AthleteDetailState(athleteId = athleteId)
), AthleteDetailScreenActions {

    init {
        viewModelScope.launch { loadAthlete() }
    }

    private suspend fun loadAthlete() {
        val athlete = athleteRepository.getAthlete(athleteId)
        if (athlete == null) {
            changeState {
                copy(
                    isLoading = false,
                    notFound = true
                )
            }
            return
        }
        val records = athleteRepository.getPersonalBests(athleteId)
            .filter { it.boatType == TrainingPieceType.SINGLE }
            .map { best ->
                PersonalBestDraft(
                    distanceMeters = best.distanceMeters,
                    timeMillis = best.timeMillis,
                    distanceText = best.distanceMeters.toString(),
                    timeText = RowingTimeFormat.formatDuration(best.timeMillis),
                )
            }
        changeState {
            copy(
                name = athlete.name,
                dateOfBirth = birthDateDigits(athlete.dateOfBirth),
                speedCoachSerial = athlete.speedCoachSerial.orEmpty(),
                isMale = athlete.isMale,
                records = records,
                isLoading = false,
                notFound = false,
            )
        }
    }

    override fun onBackClick() {
        composeNavigator.navigateBack()
    }

    override fun onSaveClick() {
        if (!validateForm()) {
            changeState {
                copy(
                    nameError = resourceUtils.getString(R.string.athletes_add_athlete_name_error)
                        .takeIf { name.isBlank() },
                    dateError = resourceUtils.getString(R.string.athletes_add_athlete_date_error)
                        .takeIf { !isValidDate(dateOfBirth) },
                    recordsError = validateRecordsError(),
                )
            }
            return
        }
        viewModelScope.launch {
            athleteRepository.updateAthlete(
                AthleteEntity(
                    id = athleteId,
                    name = state.name.trim(),
                    dateOfBirth = getBirthDate(state.dateOfBirth) ?: return@launch,
                    isMale = state.isMale,
                    speedCoachSerial = state.speedCoachSerial.trim().ifBlank { null },
                )
            )
            val bests = state.records.map { draft ->
                AthletePersonalBestEntity(
                    athleteId = athleteId,
                    boatType = TrainingPieceType.SINGLE,
                    distanceMeters = draft.distanceMeters,
                    timeMillis = RowingTimeFormat.parseDuration(draft.timeText) ?: draft.timeMillis,
                )
            }
            athleteRepository.replacePersonalBestsForBoatType(
                athleteId = athleteId,
                boatType = TrainingPieceType.SINGLE,
                bests = bests,
            )
            onBackClick()
        }
    }

    override fun changeName(newName: String) {
        changeState { copy(name = newName, nameError = null) }
    }

    override fun changeBirthDate(newDate: String) {
        if (newDate.length <= 8 && newDate.all { it.isDigit() }) {
            changeState { copy(dateOfBirth = newDate, dateError = null) }
        }
    }

    override fun changeGender(isMale: Boolean) {
        changeState { copy(isMale = isMale) }
    }

    override fun changeSpeedCoachSerial(newSerial: String) {
        changeState { copy(speedCoachSerial = newSerial.filter { it.isLetterOrDigit() }) }
    }

    override fun addRecord() {
        changeState {
            copy(records = records + PersonalBestDraft(), recordsError = null)
        }
    }

    override fun removeRecord(localId: Long) {
        changeState {
            copy(records = records.filterNot { it.localId == localId }, recordsError = null)
        }
    }

    override fun changeRecordDistance(localId: Long, text: String) {
        val digits = text.filter { it.isDigit() }
        changeState {
            copy(
                records = records.map { draft ->
                    if (draft.localId != localId) draft
                    else draft.copy(
                        distanceText = digits,
                        distanceMeters = digits.toIntOrNull() ?: 0,
                    )
                },
                recordsError = null,
            )
        }
    }

    override fun changeRecordTime(localId: Long, text: String) {
        changeState {
            copy(
                records = records.map { draft ->
                    if (draft.localId != localId) draft
                    else draft.copy(
                        timeText = text,
                        timeMillis = RowingTimeFormat.parseDuration(text) ?: 0L,
                    )
                },
                recordsError = null,
            )
        }
    }

    override fun clearRecordsError() {
        changeState { copy(recordsError = null) }
    }

    private fun validateForm(): Boolean =
        state.name.isNotBlank() && isValidDate(state.dateOfBirth) && validateRecordsError() == null

    private fun validateRecordsError(): String? {
        val distances = mutableSetOf<Int>()
        for (record in state.records) {
            if (record.distanceMeters <= 0) {
                return resourceUtils.getString(R.string.athletes_detail_record_distance_error)
            }
            val time = RowingTimeFormat.parseDuration(record.timeText) ?: record.timeMillis
            if (time <= 0) {
                return resourceUtils.getString(R.string.athletes_detail_record_time_error)
            }
            if (!distances.add(record.distanceMeters)) {
                return resourceUtils.getString(R.string.athletes_detail_duplicate_distance)
            }
        }
        return null
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            format.isLenient = false
            format.parse(dateString) != null
        } catch (_: Exception) {
            false
        }
    }

    private fun getBirthDate(dateOfBirth: String): Date? =
        SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).parse(dateOfBirth)

    private fun birthDateDigits(date: Date): String =
        SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(date)

    companion object {
        private const val DATE_PATTERN = "ddMMyyyy"
    }
}
