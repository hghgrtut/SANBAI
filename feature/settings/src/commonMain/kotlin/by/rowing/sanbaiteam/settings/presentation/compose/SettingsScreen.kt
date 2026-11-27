package by.rowing.sanbaiteam.settings.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import by.rowing.sanbaiteam.settings.data.model.AppThemeMode
import by.rowing.sanbaiteam.settings.presentation.SettingsActions
import by.rowing.sanbaiteam.settings.presentation.SettingsViewModel
import by.rowing.sanbaiteam.settings.presentation.models.SettingsUiState
import by.rowing.sanbaiteam.settings.presentation.models.WorkDisplaySettingItem
import by.rowing.sanbaiteam.settings.resources.Res
import by.rowing.sanbaiteam.settings.resources.settings_theme_auto
import by.rowing.sanbaiteam.settings.resources.settings_theme_dark
import by.rowing.sanbaiteam.settings.resources.settings_theme_hint
import by.rowing.sanbaiteam.settings.resources.settings_theme_label
import by.rowing.sanbaiteam.settings.resources.settings_theme_light
import by.rowing.sanbaiteam.settings.resources.settings_theme_section
import by.rowing.sanbaiteam.settings.resources.settings_title
import by.rowing.sanbaiteam.settings.resources.settings_work_display_hint
import by.rowing.sanbaiteam.settings.resources.settings_work_display_meters
import by.rowing.sanbaiteam.settings.resources.settings_work_display_minutes
import by.rowing.sanbaiteam.settings.resources.settings_work_display_section
import by.rowing.sanbaiteam.training.data.entity.TrainingWorkDisplayMode
import by.rowing.sanbaiteam.uikit.component.DataCard
import by.rowing.sanbaiteam.uikit.component.TopAppBar
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel) {
    SettingsScreenContent(
        state = viewModel.state,
        actions = viewModel,
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    actions: SettingsActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.settings_title),
                onNavIconClick = actions::navigateBack,
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            SettingsLoader(paddingValues = paddingValues)
        } else {
            SettingsList(
                paddingValues = paddingValues,
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun SettingsLoader(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun SettingsList(
    paddingValues: PaddingValues,
    state: SettingsUiState,
    actions: SettingsActions,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(all = Spacing.M),
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
    ) {
        item(key = "theme_header") {
            SettingsSectionHeader(
                title = stringResource(Res.string.settings_theme_section),
                hint = stringResource(Res.string.settings_theme_hint),
            )
        }
        item(key = "theme_setting") {
            ThemeSettingRow(
                mode = state.themeMode,
                onModeChanged = actions::onThemeModeChanged,
            )
        }
        item(key = "work_header") {
            SettingsSectionHeader(
                title = stringResource(Res.string.settings_work_display_section),
                hint = stringResource(Res.string.settings_work_display_hint),
            )
        }
        items(
            items = state.items,
            key = { item -> item.pieceType.name },
        ) { item ->
            WorkDisplaySettingRow(
                item = item,
                onModeChanged = { mode -> actions.onWorkDisplayModeChanged(item.pieceType, mode) },
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    hint: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.XS),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
    ) {
        Text(
            text = title,
            style = TypographyPalette.H3,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = hint,
            style = TypographyPalette.Body2Regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemeSettingRow(
    mode: AppThemeMode,
    onModeChanged: (AppThemeMode) -> Unit,
) {
    DataCard(
        modifier = Modifier.fillMaxWidth(),
        verticalSpacing = Spacing.SM,
    ) {
        Text(
            text = stringResource(Res.string.settings_theme_label),
            style = TypographyPalette.Body1Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SettingsSegmentedSelector(
            options = listOf(
                AppThemeMode.AUTO to Res.string.settings_theme_auto,
                AppThemeMode.LIGHT to Res.string.settings_theme_light,
                AppThemeMode.DARK to Res.string.settings_theme_dark,
            ),
            selected = mode,
            onSelected = onModeChanged,
        )
    }
}

@Composable
private fun WorkDisplaySettingRow(
    item: WorkDisplaySettingItem,
    onModeChanged: (TrainingWorkDisplayMode) -> Unit,
) {
    DataCard(
        modifier = Modifier.fillMaxWidth(),
        verticalSpacing = Spacing.SM,
    ) {
        Text(
            text = stringResource(item.pieceType.uiResId),
            style = TypographyPalette.Body1Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SettingsSegmentedSelector(
            options = listOf(
                TrainingWorkDisplayMode.METERS to Res.string.settings_work_display_meters,
                TrainingWorkDisplayMode.MINUTES to Res.string.settings_work_display_minutes,
            ),
            selected = item.mode,
            onSelected = onModeChanged,
        )
    }
}

@Composable
private fun <T> SettingsSegmentedSelector(
    options: List<Pair<T, StringResource>>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,
        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (option, titleRes) ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                colors = colors,
                modifier = Modifier.weight(1f),
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