package by.rowing.sanbaiteam.uikit.component

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import by.rowing.sanbaiteam.R
import by.rowing.sanbaiteam.uikit.theme.ColorPalette
import by.rowing.sanbaiteam.uikit.theme.Spacing
import by.rowing.sanbaiteam.uikit.theme.TypographyPalette
import com.checker.uikit3.modifier.ClickableState
import com.checker.uikit3.modifier.LocalClickableState
import com.checker.uikit3.modifier.clickableIfProvided

private val ICON_SIZE
    @Composable get() = 24.dp

val TOP_BAR_HEIGHT
    @Composable get() = 44.dp

val TITLE_TEXT_STYLE
    @Composable get() = TypographyPalette.Body2Medium

@OptIn(ExperimentalMaterial3Api::class)
private val DEFAULT_WINDOW_INSETS
    @Composable get() = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal)

@Composable
private fun TopAppBarInternalWithLabels(
    modifier: Modifier = Modifier,
    title: AnnotatedString,
    @DrawableRes leadingIcon: Int? = null,
    leadingText: String? = null,
    onLeadingBlockClick: (() -> Unit)? = null,
    @DrawableRes trailingIcon: Int? = null,
    trailingText: String? = null,
    onTrailingBlockClick: (() -> Unit)? = null,
    theme: TopAppBarTheme = TopAppBarTheme.TRANSPARENT
): Unit = TopAppBarInternal(
    modifier = modifier,
    title = title,
    navigationIcon = {
        Row(
            modifier = Modifier
                .padding(start = Spacing.XS)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickableIfProvided(onClick = onLeadingBlockClick)
                .padding(Spacing.S),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            if (leadingIcon != null) {
                Icon(
                    modifier = Modifier.size(ICON_SIZE),
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                )
            }

            if (leadingText != null) {
                Text(
                    text = leadingText,
                    style = TypographyPalette.Body1Regular,
                    color = ColorPalette.Grey20,
                )
            }
        }
    },
    actions = {
        Row(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickableIfProvided(onClick = onTrailingBlockClick)
                .padding(Spacing.S)
                .padding(end = Spacing.XS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = TypographyPalette.Body1Regular,
                    color = ColorPalette.Grey20,
                )
            }

            if (trailingIcon != null) {
                Icon(
                    modifier = Modifier
                        .padding(end = Spacing.S)
                        .size(ICON_SIZE),
                    painter = painterResource(trailingIcon),
                    contentDescription = null,
                )
            }
        }
    },
    theme = theme
)

@Composable
fun SimpleTopAppBar(
    title: String = "",
    theme: TopAppBarTheme = TopAppBarTheme.TRANSPARENT,
    onNavIconClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = title,
        theme = theme,
        onNavIconClick = onNavIconClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithCustomContent(
    navIcon: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentIcon: Painter? = null,
    onNavIconClick: (() -> Unit)? = null,
    theme: TopAppBarTheme = TopAppBarTheme.TRANSPARENT,
) {
    CenterAlignedTopAppBar(
        title = content ?: {
            if (contentIcon != null) {
                Image(
                    painter = contentIcon,
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = 120.dp,
                        height = 50.dp
                    ),
                )
            }
        },
        navigationIcon = navIcon ?: {
            if (onNavIconClick != null) {
                DefaultNavigationIcon(
                    onNavIconClick = onNavIconClick
                )
            }
        },
        actions = actions,
        colors = theme.colors(),
        windowInsets = DEFAULT_WINDOW_INSETS,
        expandedHeight = if (contentIcon != null) {
            TopAppBarDefaults.MediumAppBarExpandedHeight
        } else {
            TOP_BAR_HEIGHT
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String = "",
    theme: TopAppBarTheme = TopAppBarTheme.TRANSPARENT,
    actions: @Composable RowScope.() -> Unit = {},
    onNavIconClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    hasNavigationIcon: Boolean = true,
    windowInsets: WindowInsets = DEFAULT_WINDOW_INSETS,
    backIconPainter: Painter? = null,
) {
    val containerColor by animateColorAsState(
        targetValue = theme.containerColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    TopAppBarInternal(
        modifier = modifier
            .background(containerColor)
            .statusBarsPadding(),
        title = AnnotatedString(title),
        navigationIcon = navigationIcon ?: {
            if (hasNavigationIcon) {
                DefaultNavigationIcon(
                    tint = theme.colors().navigationIconContentColor,
                    onNavIconClick = onNavIconClick,
                    customIconPainter = backIconPainter
                )
            }
        },
        actions = @Composable {
            Row(
                modifier = Modifier
                    .padding(end = Spacing.S)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        },
        theme = theme,
        windowInsets = windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviourTopAppBar(
    theme: TopAppBarTheme,
    scrollBehavior: TopAppBarScrollBehavior?,
    content: @Composable () -> Unit
) {
    TopAppBar(
        title = content,
        expandedHeight = TOP_BAR_HEIGHT,
        windowInsets = DEFAULT_WINDOW_INSETS,
        colors = theme.colors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarInternal(
    modifier: Modifier = Modifier,
    title: AnnotatedString,
    theme: TopAppBarTheme = TopAppBarTheme.TRANSPARENT,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = DEFAULT_WINDOW_INSETS,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.S),
                text = title,
                style = TITLE_TEXT_STYLE,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = theme.colors(),
        windowInsets = windowInsets,
        expandedHeight = TOP_BAR_HEIGHT
    )
}

@Composable
fun DefaultNavigationIcon(
    modifier: Modifier = Modifier,
    tint: Color = ColorPalette.Grey20,
    onNavIconClick: (() -> Unit)? = null,
    customIconPainter: Painter? = null
) {
    val clickableState = LocalClickableState.current
    val activity = (LocalActivity.current as? FragmentActivity)
    val clickListener = {
        clickableState.onClick {
            onNavIconClick?.invoke() ?: activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    val defaultIconPainter = painterResource(id = R.drawable.ic_chevron_left_24)

    IconButton(
        modifier = modifier,
        onClick = clickListener,
        content = {
            Icon(
                modifier = Modifier.size(ICON_SIZE),
                painter = customIconPainter ?: defaultIconPainter,
                contentDescription = null,
                tint = tint
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
sealed interface TopAppBarTheme {
    val containerColor: Color
    val contentColor: Color

    fun colors() = TopAppBarColors(containerColor, contentColor)

    data object TRANSPARENT : TopAppBarTheme {
        override val containerColor = ColorPalette.Transparent
        override val contentColor = ColorPalette.Grey20
    }

    data object LIGHT : TopAppBarTheme {
        override val containerColor = ColorPalette.White
        override val contentColor = ColorPalette.Grey20
    }

    data object DARK : TopAppBarTheme {
        override val containerColor = ColorPalette.Grey20
        override val contentColor = ColorPalette.White
    }

    data class Custom(
        override val containerColor: Color = ColorPalette.White,
        override val contentColor: Color = ColorPalette.Grey20,
    ) : TopAppBarTheme
}

@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarColors(
    containerColor: Color,
    contentColor: Color,
    scrolledContainerColor: Color = containerColor,
): TopAppBarColors = TopAppBarColors(
    containerColor = containerColor,
    scrolledContainerColor = scrolledContainerColor,
    navigationIconContentColor = contentColor,
    titleContentColor = contentColor,
    actionIconContentColor = contentColor,
)

@Preview(
    showBackground = true,
    backgroundColor = 0x999999,
)
@Composable
private fun TopAppBarPreview() {
    CompositionLocalProvider(
        LocalClickableState provides ClickableState()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.M)
        ) {
            TopAppBar(
                title = "Title",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            modifier = Modifier.size(ICON_SIZE),
                            painter = painterResource(R.drawable.ic_check_16),
                            contentDescription = null,
                        )
                    }
                },
            )

            TopAppBarInternalWithLabels(
                title = AnnotatedString("Title2"),
                leadingIcon = R.drawable.ic_chevron_left_24,
                leadingText = "Label L",
                onLeadingBlockClick = {},
                trailingIcon = R.drawable.ic_check_16,
                trailingText = "Label R",
                theme = TopAppBarTheme.LIGHT
            )

            TopAppBar(
                title = "Long Long Long Long Long Long Long Long Long Long Long Long Long Long Title",
                theme = TopAppBarTheme.DARK,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            modifier = Modifier.size(ICON_SIZE),
                            painter = painterResource(R.drawable.ic_check_16),
                            contentDescription = null,
                        )
                    }
                }
            )

            TopAppBarInternalWithLabels(
                title = AnnotatedString("Long Long Long Long Long Long Long Long Long Long Long Long Title2"),
                leadingIcon = R.drawable.ic_chevron_left_24,
                leadingText = "Label L",
                onLeadingBlockClick = {},
                trailingIcon = R.drawable.ic_check_16,
                trailingText = "Label R",
                theme = TopAppBarTheme.LIGHT
            )

            TopAppBarWithCustomContent(
                contentIcon = painterResource(id = R.drawable.ic_check_16),
                onNavIconClick = {},
                theme = TopAppBarTheme.LIGHT
            )
        }
    }
}