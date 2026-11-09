package by.rowing.sanbaiteam.training.data

import by.rowing.sanbaiteam.training.data.entity.TrainingDetailHiddenFieldEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDetailHiddenFieldDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeTrainingDetailHiddenFieldDao(
    hiddenFields: List<String> = emptyList(),
) : TrainingDetailHiddenFieldDao {

    private val hiddenFlow = MutableStateFlow(hiddenFields)

    override fun observeHidden(): Flow<List<String>> = hiddenFlow.asStateFlow()

    override suspend fun hide(entity: TrainingDetailHiddenFieldEntity) {
        hiddenFlow.value = (hiddenFlow.value + entity.field).distinct()
    }

    override suspend fun show(field: String) {
        hiddenFlow.value -= field
    }

    override suspend fun clear() {
        hiddenFlow.value = emptyList()
    }
}