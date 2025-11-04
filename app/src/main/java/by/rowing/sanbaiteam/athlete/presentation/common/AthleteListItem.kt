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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPaletteSp
import kotlin.math.abs

@Composable
internal fun AthleteListItem(
    athlete: AthleteItemModel,
    onAthleteClick: (AthleteItemModel) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.M,
                vertical = Spacing.S
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { onAthleteClick(athlete) }
    ) {
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
                    .size(60.dp)
                    .background(
                        color = getAvatarColor(athlete.name),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = athlete.initials,
                    style = TypographyPaletteSp.H1.copy(color = Color.White),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = athlete.name,
                    style = TypographyPaletteSp.H3.copy(color = MaterialTheme.colorScheme.onSurface)
                )
                InfoChip(
                    icon = if (athlete.isMale) Icons.Default.Male else Icons.Default.Female,
                    text = athlete.gender
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.XS)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = TypographyPaletteSp.Body3Regular.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFF795548), // Brown
    )
    return colors[abs(name.hashCode()) % colors.size]
}