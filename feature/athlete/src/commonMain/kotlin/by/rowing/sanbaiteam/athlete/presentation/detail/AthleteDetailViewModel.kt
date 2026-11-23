package by.rowing.sanbaiteam.athlete.presentation.detail

import androidx.lifecycle.viewModelScope
import by.rowing.sanbaiteam.athlete.data.entity.AthleteEntity
import by.rowing.sanbaiteam.athlete.data.entity.AthletePersonalBestEntity
import by.rowing.sanbaiteam.athlete.data.repository.AthletesRepository
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightInput
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightMath
import by.rowing.sanbaiteam.athlete.domain.weight.AthleteWeightRecord
import by.rowing.sanbaiteam.athlete.presentation.detail.state.AthleteDetailState
import by.rowing.sanbaiteam.athlete.presentation.detail.state.PersonalBestDraft
import by.rowing.sanbaiteam.athlete.presentation.detail.state.WeightDialogState
import by.rowing.sanbaiteam.athlete.presentation.detail.state.WeightRangeDialogState
import by.rowing.sanbaiteam.athlete.presentation.navigation.AthletesNavigation
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_date_error
import by.rowing.sanbaiteam.athlete.resources.athletes_add_athlete_name_error
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_duplicate_distance
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_record_distance_error
import by.rowing.sanbaiteam.athlete.resources.athletes_detail_record_time_error
import by.rowing.sanbaiteam.athlete.resources.athletes_weight_range_error
import by.rowing.sanbaiteam.core.presentation.compose.ComposeNavigator
import by.rowing.sanbaiteam.core.presentation.compose.getReturnToScreenNavOptions
import by.rowing.sanbaiteam.core.presentation.screen.base.ComposeBaseViewModel
import by.rowing.sanbaiteam.core.util.RowingTimeFormat
import by.rowing.sanbaiteam.core.util.TimeUtils
import by.rowing.sanbaiteam.core.util.nowInMillis
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

    override fun navigateBack() {
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
            navigateBack()
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

    override fun openWeightDialog() {
        changeState {
            copy(
                weightDialog = WeightDialogState(
                    dateMillis = TimeUtils.noonOfDay(timeInMillis = nowInMillis()),
                    weightText = currentWeightTenths?.let { AthleteWeightInput.formatWeight(tenths = it) }.orEmpty(),
                )
            )
        }
    }

    override fun dismissWeightDialog() {
        changeState { copy(weightDialog = null) }
    }

    override fun changeWeightDate(
        year: Int,
        month: Int,
        day: Int,
    ) {
        TimeUtils.noonMillis(
            year = year,
            month = month,
            day = day,
        )?.let { dateMillis ->
            changeState {
                copy(
                    weightDialog = weightDialog?.copy(
                        dateMillis = dateMillis,
                        error = null,
                    )
                )
            }
        }
    }

    override fun changeWeightValue(text: String) {
        changeState {
            copy(
                weightDialog = weightDialog?.copy(
                    weightText = AthleteWeightInput.sanitize(text = text),
                    error = null,
                )
            )
        }
    }

    override fun saveWeight() {
        val dialog = state.weightDialog ?: return
        val weightTenths = AthleteWeightInput.parse(text = dialog.weightText)
        val error = AthleteWeightInput.validate(
            text = dialog.weightText,
            dateMillis = dialog.dateMillis,
            nowMillis = nowInMillis(),
        )
        if (error != null) {
            changeState { copy(weightDialog = dialog.copy(error = error)) }
        } else {
            viewModelScope.launch {
                athleteRepository.saveWeight(
                    athleteId = athleteId,
                    dateMillis = dialog.dateMillis,
                    weightTenths = weightTenths ?: return@launch,
                )
                loadWeights()
                changeState { copy(weightDialog = null) }
            }
        }
    }

    override fun requestDeleteWeight(dateMillis: Long) {
        changeState { copy(deleteWeightMillis = dateMillis) }
    }

    override fun dismissDeleteWeight() {
        changeState { copy(deleteWeightMillis = null) }
    }

    override fun confirmDeleteWeight() {
        val dateMillis = state.deleteWeightMillis ?: return
        viewModelScope.launch {
            athleteRepository.deleteWeight(
                athleteId = athleteId,
                dateMillis = dateMillis,
            )
            loadWeights()
            changeState { copy(deleteWeightMillis = null) }
        }
    }

    override fun toggleWeightTable() {
        changeState { copy(isWeightTableVisible = !isWeightTableVisible) }
    }

    override fun openWeightRangeDialog() {
        val today = TimeUtils.noonOfDay(timeInMillis = nowInMillis())
        changeState {
            copy(
                weightRangeDialog = WeightRangeDialogState(
                    fromMillis = weights.firstOrNull()?.dateMillis ?: today,
                    toMillis = today,
                )
            )
        }
    }

    override fun dismissWeightRangeDialog() {
        changeState { copy(weightRangeDialog = null) }
    }

    override fun changeWeightRangeFrom(
        year: Int,
        month: Int,
        day: Int,
    ) {
        val dateMillis = TimeUtils.noonMillis(
            year = year,
            month = month,
            day = day,
        ) ?: return
        changeState {
            copy(
                weightRangeDialog = weightRangeDialog?.copy(
                    fromMillis = dateMillis,
                    error = null,
                )
            )
        }
    }

    override fun changeWeightRangeTo(
        year: Int,
        month: Int,
        day: Int,
    ) {
        val dateMillis = TimeUtils.noonMillis(
            year = year,
            month = month,
            day = day,
        ) ?: return
        changeState {
            copy(
                weightRangeDialog = weightRangeDialog?.copy(
                    toMillis = dateMillis,
                    error = null,
                )
            )
        }
    }

    override fun buildWeightChart() {
        val dialog = state.weightRangeDialog ?: return
        val from = TimeUtils.startOfDayMillis(timeInMillis = dialog.fromMillis)
        val to = TimeUtils.startOfDayMillis(timeInMillis = dialog.toMillis)
        if (from > to) {
            changeState {
                copy(weightRangeDialog = dialog.copy(error = UiText.of(Res.string.athletes_weight_range_error)))
            }
            return
        }
        AthletesNavigation.AthleteWeightChart(
            athleteId = athleteId,
            fromMillis = TimeUtils.noonOfDay(timeInMillis = from),
            toMillis = TimeUtils.noonOfDay(timeInMillis = to),
        ).navigateTo(
            composeNavigator = composeNavigator,
            navOptions = getReturnToScreenNavOptions<AthletesNavigation.AthleteDetail>(),
        )
        changeState { copy(weightRangeDialog = null) }
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
        loadWeights()
    }

    private suspend fun loadWeights() {
        val weights = athleteRepository.getWeights(athleteId).map { entity ->
            AthleteWeightRecord(
                dateMillis = entity.dateMillis,
                weightTenths = entity.weightTenths,
            )
        }
        changeState {
            copy(
                weights = weights,
                currentWeightTenths = AthleteWeightMath.currentWeightTenths(
                    records = weights,
                    todayMillis = nowInMillis(),
                ),
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