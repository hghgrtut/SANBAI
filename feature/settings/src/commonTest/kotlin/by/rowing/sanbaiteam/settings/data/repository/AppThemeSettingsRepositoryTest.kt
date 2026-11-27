package by.rowing.sanbaiteam.settings.data.repository

import by.rowing.sanbaiteam.settings.data.model.AppThemeMode
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AppThemeSettingsRepositoryTest {

    @Test
    fun `uses auto theme by default`() {
        val repository = AppThemeSettingsRepository(settings = MapSettings())

        assertEquals(AppThemeMode.AUTO, repository.mode.value)
    }

    @Test
    fun `stores selected theme`() {
        val repository = AppThemeSettingsRepository(settings = MapSettings())

        repository.setMode(mode = AppThemeMode.DARK)

        assertEquals(AppThemeMode.DARK, repository.mode.value)
    }

    @Test
    fun `restores stored theme`() {
        val settings = MapSettings()

        AppThemeSettingsRepository(settings = settings).setMode(mode = AppThemeMode.LIGHT)

        assertEquals(AppThemeMode.LIGHT, AppThemeSettingsRepository(settings = settings).mode.value)
    }
}
