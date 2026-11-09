package by.rowing.sanbaiteam.training.data.repository

import by.rowing.sanbaiteam.training.data.FakeTrainingDetailHiddenFieldDao
import by.rowing.sanbaiteam.training.data.models.detail.TrainingDetailField
import by.rowing.sanbaiteam.training.data.models.detail.canToggle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TrainingDetailSettingsRepositoryTest {

    @Test
    fun `all fields are visible by default`() = runTest {
        val repository = TrainingDetailSettingsRepository(dao = FakeTrainingDetailHiddenFieldDao())

        assertTrue(repository.hiddenFields.first().isEmpty())
    }

    @Test
    fun `hidden field is stored and restored`() = runTest {
        val repository = TrainingDetailSettingsRepository(dao = FakeTrainingDetailHiddenFieldDao())

        for (field in listOf(TrainingDetailField.MAX_HEART_RATE, TrainingDetailField.LACTATE)) {
            repository.setVisible(
                field = field,
                visible = false
            )
        }

        assertEquals(
            setOf(TrainingDetailField.MAX_HEART_RATE, TrainingDetailField.LACTATE),
            repository.hiddenFields.first(),
        )
    }

    @Test
    fun `visible field is removed from hidden`() = runTest {
        val repository = TrainingDetailSettingsRepository(
            dao = FakeTrainingDetailHiddenFieldDao(
                hiddenFields = listOf(
                    TrainingDetailField.LACTATE.name,
                    TrainingDetailField.MAX_HEART_RATE.name,
                )
            )
        )

        repository.setVisible(
            field = TrainingDetailField.LACTATE,
            visible = true
        )

        assertEquals(
            setOf(TrainingDetailField.MAX_HEART_RATE),
            repository.hiddenFields.first(),
        )
    }

    @Test
    fun `show all clears hidden fields`() = runTest {
        val repository = TrainingDetailSettingsRepository(
            dao = FakeTrainingDetailHiddenFieldDao(
                hiddenFields = listOf(TrainingDetailField.LACTATE.name)
            )
        )

        repository.showAll()

        assertTrue(repository.hiddenFields.first().isEmpty())
    }

    @Test
    fun `unknown saved field is ignored`() = runTest {
        val repository = TrainingDetailSettingsRepository(
            dao = FakeTrainingDetailHiddenFieldDao(
                hiddenFields = listOf(
                    TrainingDetailField.MAX_HEART_RATE.name,
                    "UNKNOWN_FIELD",
                )
            )
        )

        assertEquals(
            setOf(TrainingDetailField.MAX_HEART_RATE),
            repository.hiddenFields.first(),
        )
    }

    @Test
    fun `last visible field cannot be hidden`() {
        val lastField = TrainingDetailField.entries.last()
        val hiddenFields = TrainingDetailField.entries.dropLast(1).toSet()

        assertFalse(hiddenFields.canToggle(field = lastField))
    }

    @Test
    fun `hidden field can always be shown`() {
        val lastField = TrainingDetailField.entries.last()
        val hiddenFields = TrainingDetailField.entries.toSet()

        assertTrue(hiddenFields.canToggle(field = lastField))
    }
}