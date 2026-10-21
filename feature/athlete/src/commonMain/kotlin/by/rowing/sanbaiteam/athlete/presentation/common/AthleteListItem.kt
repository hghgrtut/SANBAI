package by.rowing.sanbaiteam.athlete.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.athlete.data.model.AthleteItemModel
import by.rowing.sanbaiteam.athlete.resources.Res
import by.rowing.sanbaiteam.athlete.resources.athletes_age_years
import by.rowing.sanbaiteam.athlete.resources.athletes_gender_female_short
import by.rowing.sanbaiteam.athlete.resources.athletes_gender_male_short
import by.rowing.sanbaiteam.uikit.component.Badge
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
internal fun AthleteListItem(
    athlete: AthleteItemModel,
    onAthleteClick: (AthleteItemModel) -> Unit,
) {
    ItemCard(onClick = { onAthleteClick(athlete) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.M)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(AVATAR_SIZE)
                    .background(
                        color = getAvatarColor(athlete.name),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = athlete.initials,
                    style = TypographyPalette.H1,
                    color = ColorPalette.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.S)
            ) {
                Text(
                    text = athlete.name,
                    style = TypographyPalette.H3,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(space = Spacing.XS)) {
                    Badge(
                        text = stringResource(
                            if (athlete.isMale) {
                                Res.string.athletes_gender_male_short
                            } else {
                                Res.string.athletes_gender_female_short
                            }
                        ),
                        icon = if (athlete.isMale) Icons.Default.Male else Icons.Default.Female,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    athlete.age?.let { age ->
                        Badge(
                            text = pluralStringResource(
                                Res.plurals.athletes_age_years,
                                age,
                                age,
                            )
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}

private val AVATAR_SIZE = 60.dp
private val ICON_SIZE = 20.dp

/**
 * Набор брендовых цветов с достаточным контрастом для белых инициалов.
 */
private val AVATAR_COLORS = listOf(
    ColorPalette.Blue50,
    ColorPalette.Blue30,
    ColorPalette.Blue20,
    ColorPalette.Grey20,
    ColorPalette.Grey30,
    ColorPalette.Red50,
)

private fun getAvatarColor(name: String): Color =
    AVATAR_COLORS[abs(name.hashCode()) % AVATAR_COLORS.size]
