package by.rowing.sanbaiteam.athlete.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.R
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

    private val dateFormat: SimpleDateFormat get() = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

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
                    dateOfBirth = dateFormat.parse(state.dateOfBirth) ?: return@launch,
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
        if (newDate.length <= 8 && newDate.all { it.isDigit() }) {
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
                        timeMillis = RowingTimeFormat.parseDuration(text) ?: 0L,
                    )
                }
            }
        }
    }

    override fun clearRecordsError() {
        changeState { copy(recordsError = null) }
    }

    private suspend fun savePersonalBests() {
        val bests = TrainingPieceType.entries.flatMap { type ->
            state.recordsByType[type].orEmpty().map { draft ->
                AthletePersonalBestEntity(
                    athleteId = athleteId,
                    boatType = type,
                    distanceMeters = draft.distanceMeters,
                    timeMillis = RowingTimeFormat.parseDuration(draft.timeText) ?: draft.timeMillis,
                )
            }
        }
        athleteRepository.replacePersonalBests(athleteId = athleteId, bests = bests)
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
                dateOfBirth = dateFormat.format(athlete.dateOfBirth),
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

    private fun validateRecordsError(): String? {
        for (records in state.recordsByType.values) {
            val error = validateRecords(records)
            if (error != null) return error
        }
        return null
    }

    private fun validateRecords(records: List<PersonalBestDraft>): String? {
        val distances = mutableSetOf<Int>()
        for (record in records) {
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
            val format = dateFormat
            format.isLenient = false
            format.parse(dateString) != null
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val DATE_PATTERN = "ddMMyyyy"
    }
}
