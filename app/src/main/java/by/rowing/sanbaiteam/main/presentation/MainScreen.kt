package by.rowing.sanbaiteam.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.rowing.sanbaiteam.uikit.ComposePreviewWrapper
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerS
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXL
import by.rowing.sanbaiteam.uikit.theme.Spacing.SpacerXS
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette

@Composable
internal fun MainScreen(viewModel: MainViewModel) { MainScreenContent(viewModel) }

@Composable
private fun MainScreenContent(
    actions: MainScreenActions
) {
    Scaffold(topBar = { MainScreenTopBar() }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ColorPalette.Grey95)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WelcomeSection()
                SpacerXL()
                SpacerXL()
                NavigationGrid(actions = actions)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Sanbai Rowing",
                style = TypographyPalette.H3,
                color = ColorPalette.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ColorPalette.Blue50,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = ColorPalette.White,
            actionIconContentColor = Color.Unspecified
        )
    )
}

@Composable
private fun WelcomeSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = ColorPalette.Blue50,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBoat,
                contentDescription = "Rowing App",
                modifier = Modifier.size(60.dp),
                tint = ColorPalette.White
            )
        }
        SpacerL()
        Text(
            text = "Welcome to Sanbai Team",
            style = TypographyPalette.H2,
            color = ColorPalette.Grey10,
            textAlign = TextAlign.Center
        )
        SpacerS()
        Text(
            text = "Track your training, manage athletes, and analyze performance",
            style = TypographyPalette.Body1Regular,
            color = ColorPalette.Grey40,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.White,
            contentColor = ColorPalette.Grey10
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing._2XS),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.M),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "12",
                label = "Athletes",
                icon = Icons.Default.People,
                iconColor = ColorPalette.Blue50
            )

            StatItem(
                value = "24",
                label = "Sessions",
                icon = Icons.Default.FitnessCenter,
                iconColor = ColorPalette.Green50
            )

            StatItem(
                value = "85km",
                label = "Distance",
                icon = Icons.Default.Speed,
                iconColor = ColorPalette.Orange50
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        SpacerXS()
        Text(
            text = value,
            style = TypographyPalette.H3,
            color = ColorPalette.Grey20
        )
        Text(
            text = label,
            style = TypographyPalette.Body2Regular,
            color = ColorPalette.Grey50
        )
    }
}

@Composable
private fun NavigationGrid(
    actions: MainScreenActions
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationCard(
            title = "Team Athletes",
            subtitle = "Manage your rowing team",
            icon = Icons.Default.People,
            iconBackgroundColor = ColorPalette.Blue50,
            textColor = ColorPalette.White,
            onClick = actions::navigateToAthletesScreen,
            isHighlighted = true
        )
        NavigationCard(
            title = "Training Sessions",
            subtitle = "Log and view workouts",
            icon = Icons.Default.FitnessCenter,
            iconBackgroundColor = ColorPalette.Green50,
            textColor = ColorPalette.Grey10,
            onClick = actions::navigateToTrainingsScreen
        )
    }
}

@Composable
private fun NavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    isHighlighted: Boolean = false,
    enabled: Boolean = true
) {
    val cardColor = when {
        isHighlighted -> ColorPalette.Blue50
        enabled -> ColorPalette.White
        else -> ColorPalette.Grey90
    }
    val contentColor = when {
        isHighlighted -> ColorPalette.White
        enabled -> textColor
        else -> ColorPalette.Grey60
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) Spacing.S else Spacing._2XS
        ),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (enabled) iconBackgroundColor else ColorPalette.Grey70,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(30.dp),
                    tint = if (isHighlighted) ColorPalette.White else ColorPalette.White
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XS)
            ) {
                Text(
                    text = title,
                    style = TypographyPalette.H3,
                )
                Text(
                    text = subtitle,
                    style = TypographyPalette.Body2Medium,
                    color = when {
                        isHighlighted -> ColorPalette.White.copy(alpha = 0.9f)
                        enabled -> ColorPalette.Grey40
                        else -> ColorPalette.Grey60
                    }
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to $title",
                modifier = Modifier.size(24.dp),
                tint = when {
                    isHighlighted -> ColorPalette.White
                    enabled -> ColorPalette.Grey50
                    else -> ColorPalette.Grey60
                }
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    ComposePreviewWrapper { MainScreenContent(actions = MainScreenActions.empty()) }
}