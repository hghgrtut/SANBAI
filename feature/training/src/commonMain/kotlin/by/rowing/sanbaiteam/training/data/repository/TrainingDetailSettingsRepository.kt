package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.entity.TrainingDetailHiddenFieldEntity
import by.rowing.sanbaiteam.training.data.local.TrainingDetailHiddenFieldDao
import by.rowing.sanbaiteam.training.data.models.detail.TrainingDetailField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Хранит набор показателей, скрытых пользователем на экране детали тренировки.
 *
 * В базе лежат только скрытые показатели, поэтому отсутствие записи означает,
 * что показатель отображается. Неизвестные значения (например, из старой версии
 * приложения) игнорируются.
 */
internal class TrainingDetailSettingsRepository(
    private val dao: TrainingDetailHiddenFieldDao,
) {

    val hiddenFields: Flow<Set<TrainingDetailField>> = dao.observeHidden().map { names ->
        val knownFields = TrainingDetailField.entries.associateBy { field -> field.name }
        names.mapNotNullTo(mutableSetOf()) { name -> knownFields[name] }
    }

    suspend fun setVisible(
        field: TrainingDetailField,
        visible: Boolean,
    ) {
        if (visible) {
            dao.show(field = field.name)
        } else {
            dao.hide(entity = TrainingDetailHiddenFieldEntity(field = field.name))
        }
    }

    suspend fun showAll() {
        dao.clear()
    }
}
