package by.rowing.sanbaiteam.uikit.modifier

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

/**
 * Отступы контента экрана внутри [androidx.compose.material3.Scaffold]: применяет [paddingValues],
 * помечает их использованными и поднимает контент над экранной клавиатурой.
 *
 * [consumeWindowInsets] обязателен: нижний инсет навигационной панели уже входит в [paddingValues],
 * а IME-инсет тоже его включает. Без него отступы складываются и над клавиатурой появляется
 * лишняя пустая полоса.
 */
@AllowDetektPublic
fun Modifier.scaffoldContentInsets(paddingValues: PaddingValues): Modifier = this
    .padding(paddingValues)
    .consumeWindowInsets(paddingValues)
    .imePadding()