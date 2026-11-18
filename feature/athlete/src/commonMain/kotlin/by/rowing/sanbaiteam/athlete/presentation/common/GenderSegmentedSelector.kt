package by.rowing.sanbaiteam.athlete.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_gender_female_short
import by.rowing.sanbaiteam.athlete.resources.athletes_gender_male_short
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.stringResource

/** Компактный переключатель пола для строки с датой рождения. */
@Composable
internal fun GenderSegmentedSelector(
    isMale: Boolean,
    onGenderChange: (isMale: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        true to Res.string.athletes_gender_male_short,
        false to Res.string.athletes_gender_female_short,
    )
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,
        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (male, titleRes) ->
            SegmentedButton(
                selected = isMale == male,
                onClick = { onGenderChange(male) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                colors = colors,
                label = {
                    Text(
                        text = stringResource(titleRes),
                        style = TypographyPalette.Body2Medium,
                    )
                },
            )
        }
    }
}