package by.rowing.sanbaiteam.uikit.component.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.resources.Res
import by.rowing.sanbaiteam.uikit.resources.uikit_add
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

private val ADD_ICON_SIZE = 18.dp

/** Компактная текстовая кнопка «Добавить» с плюсом. */
@AllowDetektPublic
@Composable
fun AddTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(ADD_ICON_SIZE),
        )
        SpacerXS()
        Text(
            text = stringResource(Res.string.uikit_add),
            style = TypographyPalette.Body2Medium,
        )
    }
}