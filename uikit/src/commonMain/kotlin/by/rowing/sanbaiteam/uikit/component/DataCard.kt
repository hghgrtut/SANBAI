package by.rowing.sanbaiteam.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.uikit.theme.Spacing

@AllowDetektPublic
@Composable
fun DataCard(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) { content() }
    }
}