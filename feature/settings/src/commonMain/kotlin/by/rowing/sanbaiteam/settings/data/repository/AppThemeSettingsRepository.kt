package by.rowing.sanbaiteam.settings.data.repository

import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.settings.data.model.AppThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит выбранную тему оформления в системном key-value хранилище
 * (SharedPreferences на Android, NSUserDefaults на iOS).
 * Если пользователь ничего не выбирал, отдаёт [AppThemeMode.AUTO] — как в системе.
 */
@AllowDetektPublic
class AppThemeSettingsRepository(private val settings: Settings) {

    private val _mode = MutableStateFlow(readMode())
    val mode: StateFlow<AppThemeMode> = _mode.asStateFlow()

    fun setMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.name)
        _mode.value = mode
    }

    private fun readMode(): AppThemeMode = settings.getStringOrNull(KEY_THEME_MODE)
        ?.let { stored -> AppThemeMode.entries.firstOrNull { mode -> mode.name == stored } }
        ?: AppThemeMode.AUTO

    private companion object {

        const val KEY_THEME_MODE = "app_theme_mode"
    }
}