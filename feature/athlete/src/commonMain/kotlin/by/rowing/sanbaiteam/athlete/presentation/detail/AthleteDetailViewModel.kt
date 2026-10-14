package by.rowing.sanbaiteam.athlete.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_date_error
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_error
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_duplicate_distance
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_record_distance_error
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_record_time_error
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.training.data.entity.TrainingPieceType
import by.rowing.sanbaiteam.uikit.component.UiText
import kotlinx.coroutines.launch

internal class AthleteDetailViewModel(
    private val athleteRepository: AthletesRepository,
    private val composeNavigator: ComposeNavigator,
    private val athleteId: Long,
) : ComposeBaseViewModel<AthleteDetailState>(
    initialState = AthleteDetailState(athleteId = athleteId)
), AthleteDetailScreenActions {

    init {
        viewModelScope.launch { loadAthlete() }
    }

    override fun onBackClick() {
        composeNavigator.navigateBack()
    }

    override fun onSaveClick() {
        if (!validateForm()) {
            changeState {
                copy(
                    nameError = UiText.of(Res.string.athletes_add_athlete_name_error)
                        .takeIf { name.isBlank() },
                    dateError = UiText.of(Res.string.athletes_add_athlete_date_error)
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
                    dateOfBirth = TimeUtils.parseDateDigits(state.dateOfBirth) ?: return@launch,
                    isMale = state.isMale,
                    speedCoachSerial = state.speedCoachSerial.trim().ifBlank { null },
                )
            )
            savePersonalBests()
            onBackClick()
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
        changeState { copy(speedCoachSerial = newSerial.filter { it.isLetterOrDigit() }) }
    }

    override fun addRecord(type: TrainingPieceType) {
        updateRecords(type) { it + PersonalBestDraft() }
    }

    override fun removeRecord(
        type: TrainingPieceType,
        localId: Long
    ) {
        updateRecords(type) { records -> records.filterNot { it.localId == localId } }
    }

    override fun changeRecordDistance(
        type: TrainingPieceType,
        localId: Long,
        text: String,
    ) {
        val digits = text.filter { it.isDigit() }
        updateRecords(type) { records ->
            records.map { draft ->
                if (draft.localId != localId) {
                    draft
                } else {
                    draft.copy(
                        distanceText = digits,
                        distanceMeters = digits.toIntOrNull() ?: 0,
                    )
                }
            }
        }
    }

    override fun changeRecordTime(
        type: TrainingPieceType,
        localId: Long,
        text: String,
    ) {
        updateRecords(type) { records ->
            records.map { draft ->
                if (draft.localId != localId) {
                    draft
                } else {
                    draft.copy(
                        timeText = text,
                        timeMillis = RowingTimeFormat.parseTimeInput(input = text) ?: 0L,
                    )
                }
            }
        }
    }

    override fun clearRecordsError() {
        changeState { copy(recordsError = null) }
    }

    private suspend fun savePersonalBests() {
        athleteRepository.replacePersonalBests(
            athleteId = athleteId,
            bests = TrainingPieceType.entries.flatMap { type ->
                state.recordsByType[type].orEmpty().map { draft ->
                    AthletePersonalBestEntity(
                        athleteId = athleteId,
                        boatType = type,
                        distanceMeters = draft.distanceMeters,
                        timeMillis = RowingTimeFormat.parseTimeInput(input = draft.timeText) ?: draft.timeMillis,
                    )
                }
            }
        )
    }

    private fun updateRecords(
        type: TrainingPieceType,
        transform: (List<PersonalBestDraft>) -> List<PersonalBestDraft>,
    ) {
        changeState {
            copy(
                recordsByType = recordsByType + (type to transform(recordsByType[type].orEmpty())),
                recordsError = null,
            )
        }
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
        val recordsByType = loadRecordsByType(athleteRepository.getPersonalBests(athleteId))
        changeState {
            copy(
                name = athlete.name,
                dateOfBirth = TimeUtils.formatDateDigits(athlete.dateOfBirth),
                speedCoachSerial = athlete.speedCoachSerial.orEmpty(),
                isMale = athlete.isMale,
                recordsByType = recordsByType,
                isLoading = false,
                notFound = false,
            )
        }
    }

    private fun loadRecordsByType(
        bests: List<AthletePersonalBestEntity>,
    ): Map<TrainingPieceType, List<PersonalBestDraft>> {
        val grouped = bests.groupBy { it.boatType }
        return TrainingPieceType.entries.associateWith { type ->
            grouped[type].orEmpty()
                .sortedBy { it.distanceMeters }
                .map { best ->
                    PersonalBestDraft(
                        distanceMeters = best.distanceMeters,
                        timeMillis = best.timeMillis,
                        distanceText = best.distanceMeters.toString(),
                        timeText = RowingTimeFormat.formatDuration(best.timeMillis),
                    )
                }
        }
    }

    private fun validateForm(): Boolean =
        state.name.isNotBlank() && isValidDate(state.dateOfBirth) && validateRecordsError() == null

    private fun validateRecordsError(): UiText? = state.recordsByType.values.firstNotNullOfOrNull { records ->
        val distances = mutableSetOf<Int>()
        for (record in records) {
            if (record.distanceMeters <= 0) {
                return@firstNotNullOfOrNull UiText.of(Res.string.athletes_detail_record_distance_error)
            }
            if ((RowingTimeFormat.parseTimeInput(input = record.timeText) ?: record.timeMillis) <= 0) {
                return@firstNotNullOfOrNull UiText.of(Res.string.athletes_detail_record_time_error)
            }
            if (!distances.add(record.distanceMeters)) {
                return@firstNotNullOfOrNull UiText.of(Res.string.athletes_detail_duplicate_distance)
            }
        }
        return@firstNotNullOfOrNull null
    }

    private fun isValidDate(dateString: String): Boolean = TimeUtils.parseDateDigits(dateString) != null

    companion object {

        private const val DATE_LENGTH = 8
    }
}
