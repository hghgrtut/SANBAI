# uikit — дизайн-система проекта

Модуль `uikit` (`by.rowing.sanbaiteam.uikit`). Всё, что можно переиспользовать в UI, лежит здесь — не создавайте
локальные копии кнопок/полей/карточек в фичах.

Базовый путь: `uikit/src/commonMain/kotlin/by/rowing/sanbaiteam/uikit/`

---

## 1. Тема

`theme/Theme.kt` — `SANBAITeamTheme(useLightStatusBars?, useLightNavigationBars?, shapes?, content)`.
Светлая/тёмная схема заданы вручную; используется `isSystemInDarkTheme()`. Формы: `extraSmall/small = 8.dp`, `medium/large = 12.dp`, `extraLarge = 24.dp`.

| Файл | Что внутри |
|---|---|
| `theme/Spacing.kt` | `Spacing.None, _2XS(2), XS(4), S(8), SM(12), M(16), ML(20), L(24), XL(32), _3XL(48), _4XL(64), _5XL(80)` + `SpacerXS()…` для `ColumnScope`/`RowScope` |
| `theme/Radius.kt` | `Radius.S(8), M(12), _2XL(24)` (internal) |
| `theme/ColorPalette.kt` | `White, Transparent, Grey95…Grey10, Red70/50, Orange50, Yellow50/Yellow50New, Green50/30, Blue95/60/50/30/20` |
| `theme/TypographyPalette.kt` | `H1…H5`, `Body1Regular/Medium`, `Body2Regular/Medium`, `Body3Regular/Medium`, `Caption1Regular/Medium`, `Caption2`; `TypographyPalette.lorem` для превью |
| `theme/TypographyPaletteSp.kt` | То же для `sp`-типографики (используется в `Button`) |
| `theme/Theme.android.kt` / `.ios.kt` | `PlatformSystemBars` actual |

В фичах брать цвета из `MaterialTheme.colorScheme` (primary, surface, onSurfaceVariant, outline…), а не `ColorPalette` напрямую.
Шрифт — Inter (`Type.kt`, ресурсы `uikit/src/commonMain/composeResources/font/`).

---

## 2. Компоненты

Все публичные — с `@AllowDetektPublic`, многие с `@Preview` через `ComposePreviewWrapper { SANBAITeamTheme { … } }`.

### Кнопки и действия
`component/button/Button.kt`
```kotlin
Button(
    modifier, text: String, debounceClick: () -> Unit,
    enabled, loading, leadingIcon: @Composable (Button.() -> Unit)?,
    trailingIcon, size: ButtonSize = LARGE, buttonColors: ButtonColors = ButtonColors.default,
)
```
- `ButtonSize`: `LARGE` (24.dp радиус, основной CTA), `MEDIUM`, `SMALL`.
- `ButtonColors.primary(...)`, `ButtonColors.secondary(...)`, `ButtonColors.text(...)` — фабрики; `ButtonColors.default` = `primary`.
- Внутри уже debounce, иконки/лоадер/throbber. Не оборачивать в `clickable`.
- Инлайн-действия в карточках/списках — Material3 `TextButton` (правило AGENTS.md).

`component/BackAction.kt`, `component/BackHandler.kt` — `expect BackHandler(enabled, onBack)` (Android — системный back, iOS — `IosBackGestureHandler` в `iosMain`).

### Поля ввода
`component/TextField.kt` — три перегрузки, общий набор параметров: `value, onValueChange, label, placeholder, enabled, readOnly, error, supportingText, leadingIcon, trailingIcon, singleLine, colors, contentPadding, textStyle, keyboardOptions, visualTransformation`.
Для числовых клавиатур — `component/KeyboardOptionsExt.kt`: `KeyboardOptions.time()`, `.decimal()`, `.number(imeAction)`.

`component/RangeFields.kt` — «От/До» с обычными полями (`fromValue, onFromValueChange, toValue, onToValueChange, label, fromLabel, toLabel, fromPlaceholder, toPlaceholder, keyboardOptions, isError, supportingText`).
`component/DateRangeFields.kt` — «От/До» с диалогами дат (`fromValue, onFromClick, toValue, onToClick, label, …, isError, supportingText`).

### Выбор и списки
- `component/Checkbox.kt` — две перегрузки: с `label`/`subtitle` (строка) и без; `checked: CheckedState { UNCHECKED, CHECKED, INDETERMINATE }`, `error`, `enabled`, `click`.
- `component/Chip.kt` — `Chip(text, onClick, selected, enabled, leadingIcon, onLongClick, longClickLabel, onClearClick, clearContentDescription)`.
- `component/ChipRow.kt` — горизонтальный скроллируемый контейнер для `Chip` (`contentPadding`, `spacing`).
- `component/MultiSelectList.kt` — `MultiSelectList<T>(items, selectedItems, onItemToggle, itemLabel, itemSubtitle?, itemKey?, maxHeight = 360.dp, emptyText?)`. Используется в фильтрах (атлеты, типы).
- `component/SingleSelectDropdown.kt` — `SingleSelectDropdown<T>(items, itemText, onItemSelected, selectedItem?, label?, placeholder?, leadingIcon?, error?)` — выпадающий список с одним выбором (тип тренировки, спортсмен).
- `component/BottomSheet.kt` — `BottomSheet(onDismissRequest, title?, subtitle?, footer?, content: ColumnScope.() -> Unit)`.

### Карточки и текст
- `component/DataCard.kt` — `DataCard(modifier, verticalSpacing, content: ColumnScope.() -> Unit)`.
- `component/StatCard.kt` — `StatCard(value, label, icon?, accentColor, onClick?)` (дашборд).
- `component/SectionHeader.kt` — `SectionHeader(title, subtitle?, trailing?)`.
- `component/MetricRow.kt` — `MetricRow(label, value)`.
- `component/Badge.kt` — `Badge(text, icon?, containerColor, contentColor)`.
- `component/EmptyState.kt` — `EmptyState(title, description?, icon?, actionText?, onActionClick?)`.
- `component/Skeleton.kt` — `Skeleton(modifier, shape = MaterialTheme.shapes.small)` (загрузка списков).
- `component/Throbber.kt` — `Throbber(modifier, style: Throbber.Style, color, trackColor)`; `Throbber.Style { GREY, YELLOW }`.
- `component/UiText.kt` — `UiText.of(StringResource, vararg args)`, `@Composable resolve()`. Для ошибок/уведомлений в state.

### Даты
- `component/DatePicker.kt` — `rememberDatePicker(initialDateMillis, onDatePicked: (year, month, day) -> Unit): () -> Unit` — системный выбор даты (Android `DatePickerDialog`, iOS Material3 `DatePickerDialog`). Месяц 1–12.

### Топ-бар
`component/TopAppBar.kt`:
- `TopAppBar(modifier, title, theme, actions, onNavIconClick, navigationIcon, hasNavigationIcon, windowInsets, backIconPainter)`
- `SimpleTopAppBar(title, theme, onNavIconClick)`
- `TopAppBarWithCustomContent(navIcon, content, actions, contentIcon, onNavIconClick, theme)`
- `TopAppBarTheme { TRANSPARENT, LIGHT, DARK }`.

### Графики
- `component/chart/MetricLineChart.kt` — `MetricLineChart(points: List<MetricChartPoint>, durationMillis, valueFormatter, timeFormatter, averageValue?, reverseValueAxis, valueBounds?, timeTicks?, connectAllPoints = false)`.
  Layout транспонирован: время растёт сверху вниз, значения слева направо.
  `timeTicks` — свои отметки оси времени (обязательно для дат), `connectAllPoints` — рисовать одну линию по всем точкам без разрывов.
- `component/chart/models/MetricChartPoint.kt` — `@AllowDetektPublic data class MetricChartPoint(elapsedMillis: Long, value: Double)`.
- `component/chart/models/ChartRange.kt` — `ChartRange(min, max, step)`.
- `component/chart/MetricChartMath.kt` (internal): `niceRange`, `boundedRange`, `timeTickStep`, `buildContinuousSegments` (разрывы в данных, downsampling). Тест: `uikit/src/commonTest/.../MetricChartMathTest.kt`.

---

## 3. Модификаторы

`modifier/Modifiers.kt`:
- `Modifier.debounceClickable(interactionSource?, enabled, debounceDelayMs = 300, onClick)`
- `Modifier.debounceClickable(interactionSource, indication, enabled, debounceDelayMs, onClick)`
- `Modifier.debounceClickableWithoutEffect(onClick)` — без ripple
- `Modifier.clickableIfProvided(onClick: (() -> Unit)?)` — кликабельно только если колбэк != null
- `Modifier.applyIfEnabled(enabled: Boolean?, builder)`

`modifier/ScreenInsets.kt`:
- `Modifier.scaffoldContentInsets(paddingValues)` — padding + `consumeWindowInsets` + `imePadding()`. **Обязательно** для контента внутри `Scaffold`.

`modifier/ClickableState.kt` + `LocalClickableState.kt` — общий debounce (300 мс), `ClickableState` создаётся в Koin (`sharedAppModule`), провайдится в `App.kt`.
В `@Preview` нужен `ComposePreviewWrapper` (`uikit/Util.kt`), он подставляет `ClickableState`.