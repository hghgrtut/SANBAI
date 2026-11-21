# SANBAI Team — карта проекта (для AI-агента)

Читать в первую очередь. Здесь: слои, точки входа, навигация, данные, «куда идти за типовой задачей».
Детали по областям: [uikit.md](uikit.md) (компоненты/тема) и [training.md](training.md) (тренировки: импорт, графики, сравнение, фильтры).
Все пути — от корня репозитория. Сокращение `...` в пути означает стандартный хвост исходников модуля:
`<module>/src/commonMain/kotlin/by/rowing/sanbaiteam/<feature>/` (например, `feature/training/.../presentation/list/` =
`feature/training/src/commonMain/kotlin/by/rowing/sanbaiteam/training/presentation/list/`).

---

## 1. Что это

Kotlin Multiplatform + Compose Multiplatform (Android + iOS). UI на русском.
Room (KMP) + Koin + JetBrains navigation-compose + kotlinx-serialization + kotlinx-datetime.

Версии: Kotlin 2.4.21, Compose MP 1.10.0, AGP 9.4.1, Room 2.8.5, Koin 4.2.2, compileSdk 37, minSdk 25.
Каталог версий — `gradle/libs.versions.toml` (там же `compileSdk`/`minSdk`/`targetSdk`/`jvmTarget`).
Конвеншн-плагины: KMP-плагины модулей (`honest-sign-kmp-library`, `honest-sign-kmp-compose`, `honest-sign-kmp-feature`)
и detekt-анализ+правила (`gradle/plugins/convention`) .

**Не запускать Gradle/тесты** — верификацию делает пользователь (AGENTS.md, `opencode.json`).

---

## 2. Модули и зависимости

```
app (Android: MainActivity, RowingApp, manifest, res)
 └─ shared ────────────── App()/NavHost, MainScreen, DI, ExternalFileImport
     ├─ feature:training ─── (зависит также от feature:athlete и feature:settings)
     ├─ feature:athlete
     ├─ feature:settings
     ├─ feature:calculator
     ├─ uikit ───────────── компоненты, тема, модификаторы, UiText
     ├─ core:navigation ─── ComposeNavigator, NavigationDestination, composable<>
     ├─ core:base ───────── ComposeBaseViewModel / ComposeStateHolder
     ├─ core:file ───────── TextFileAccess, системные JSON-пикеры файлов (Android/iOS)
     ├─ core:util ───────── форматтеры времени/чисел/дат, расчёты PB
     ├─ core:annotation ─── @AllowDetektPublic
     └─ data:local ──────── Room-БД, entity, DAO, AthletesRepository (интерфейс), DataModule
iosApp (Xcode) → framework Shared (shared/src/iosMain/.../MainViewController.kt)
```

| Модуль | Назначение | Особенности |
|---|---|---|
| `app` | Android-обёртка | `MainActivity.kt`, `RowingApp.kt` (startKoin), тема/strings для Android |
| `shared` | Корневой экран, навигационный хост, DI приложения | `App.kt` — единственный NavHost; `publicResClass = true` |
| `core/navigation` | Обёртка над navigation-compose | `composable<T, VM>` + авто-`BackHandler` для `NavigateBackAware` |
| `core/base` | Базовая ViewModel | `ComposeBaseViewModel<T>` + `changeState {}` |
| `core/file` | Общий файловый слой | `TextFileAccess` (Android URI / iOS путь), `rememberCreateTextFileLauncher`, `rememberOpenTextFileLauncher`, `rememberOpenFilesLauncher` |
| `core/util` | Форматтеры и расчёты | Всё, что связано с `m:ss.t`, темпом, датами, % от PB |
| `core/annotation` | `@AllowDetektPublic` | Пометка намеренно публичных top-level объявлений |
| `data/local` | Room + DAO + entities + `AthletesRepository` | Версия БД = 6, миграции внутри `RowingDatabase` |
| `uikit` | Дизайн-система | `Spacing`, `ColorPalette`, `TypographyPalette`, `Button`, `TextField`… |
| `feature/training` | Самый большой домен | Импорт FIT/CSV, графики, сплиты, сравнение, фильтры, бэкап |
| `feature/athlete` | Список/добавление/деталь спортсмена | PB-рекорды по типам лодок, история веса и график среднего веса |
| `feature/settings` | Настройки отображения объёма | Режим метров/минут по типу тренировки |
| `feature/calculator` | Калькулятор % от рекорда ↔ темп | Только одна вьюмодель, без `*Actions`-интерфейса (легаси) |

### Новый KMP-модуль

Настройки (KMP, Android `compileSdk`/`minSdk`, `jvmTarget`, iOS-таргеты, `kotlin-test`) не копируются в модуль —
их даёт конвеншн-плагин из `gradle/plugins/convention`. В `build.gradle.kts` остаётся только уникальное:

```kotlin
import plugin.iosFramework

plugins {
    id("honest-sign-kmp-feature")   // compose-модуль без экранных зависимостей → honest-sign-kmp-compose
}                                   //                         data/утилитарный → honest-sign-kmp-library

kotlin {
    android { namespace = "by.rowing.sanbaiteam.newfeature" }
    iosFramework("NewFeature")      // только если модулю нужен собственный iOS-framework
}
```

`withHostTest {}` добавляется в `android { }` вручную — оно включает JVM-прогон `commonTest`.
У модулей с каталогом `src/commonMain/composeResources` обязательно: `implementation(compose.components.resources)`
в `commonMain` **и** `compose.resources { packageOfResClass = "by.rowing.sanbaiteam.<модуль>.resources" }`
(без явного пакета Compose берёт `project.group`/`project.name`, а в имени корня есть пробел — DEX такое не примет).
`compileSdk`/`minSdk`/`targetSdk`/`jvmTarget` меняются одной строкой в `gradle/libs.versions.toml`.

---

## 3. Точки входа

- Android: `app/src/main/java/by/rowing/sanbaiteam/MainActivity.kt` → `shared.App(importUri)`;
  DI: `app/src/main/java/by/rowing/sanbaiteam/RowingApp.kt` (`sharedAppModule, athleteModule, calculatorModule, databaseModule, trainingModule, settingsModule`).
- iOS: `shared/src/iosMain/kotlin/by/rowing/sanbaiteam/shared/MainViewController.kt` (`mainViewController()`, те же Koin-модули),
  `iosApp/iosApp/*.swift`; внешние файлы приходят через `shared/.../fileimport/ExternalFileImport.kt`.
- Корневой UI: `shared/src/commonMain/kotlin/by/rowing/sanbaiteam/shared/App.kt`
  — `SANBAITeamTheme`, `CompositionLocalProvider(LocalClickableState)`, `NavHost(start = MainNavigation.Root)`, `navigationBuilder` со всеми NavGraphProvider.
- Внешний импорт файла: `App.kt` → `TrainingNavGraphProvider.getAddTrainingRoute(uri)` (маршрут `TrainingAdd(importUri)`).

---

## 4. Навигация

`core/navigation/src/commonMain/kotlin/by/rowing/sanbaiteam/core/presentation/compose/`:

| Файл | Что даёт |
|---|---|
| `ComposeNavigator.kt` | `navigateTo / navigateBack / clearBackStack`, `NavigationEvent`, `NavigationDataArgs`. Replay-кэш; UI должен вызвать `attachUi()`/`detachUi()` (уже сделано в `App.kt`) |
| `NavigationDestination.kt` | `interface NavigationDestination<OutArg>` + `getReturnToScreenNavOptions<Parent>()` (popUpTo + singleTop) |
| `NavigationExtensions.kt` | `NavGraphBuilder.composable<T, VM>(viewModelFactory) {}` — сам вешает `BackHandler`, если VM реализует `NavigateBackAware` |
| `NavigateBackAware.kt` | Интерфейс «экран сам обрабатывает системный назад» |
| `ComposeNavExt.kt` | `CollectArgResult<T>(argName, …)` — приём результата через `savedStateHandle` (сейчас не используется) |

Маршруты (все `internal sealed interface … : NavigationDestination<Unit>`):

| Маршрут | Файл маршрутов | Экран |
|---|---|---|
| `MainNavigation.Root` | `shared/.../main/presentation/navigation/MainNavigation.kt` | Дашборд |
| `AthletesNavigation.AthletesList / AthletesAdd / AthleteDetail(id) / AthleteWeightChart(id, fromMillis, toMillis)` | `feature/athlete/.../presentation/navigation/AthletesNavigation.kt` | атлеты, график веса |
| `TrainingNavigation.TrainingList / TrainingAdd(importUri) / TrainingDetail(id) / TrainingCompare(id) / TrainingChart(id, order, metric)` | `feature/training/.../presentation/navigation/TrainingNavigation.kt` | тренировки |
| `SettingsNavigation.Settings` | `feature/settings/.../presentation/navigation/SettingsNavigation.kt` | настройки |
| `CalculatorNavigation.Calculator` | `feature/calculator/.../presentation/navigation/CalculatorNavigation.kt` | калькулятор |

Паттерн навигации: `<Feature>NavGraphProvider.<feature>NavGraph(builder, navigator)` регистрируется в `App.kt`;
из вьюмодели вызывают `TrainingNavigation.TrainingDetail(id).navigateTo(composeNavigator, getReturnToScreenNavOptions<TrainingNavigation.TrainingList>())`
или хелпер `XNavGraphProvider.navigateTo…`.
Аргументы экрана передаются параметрами Koin: `koinViewModel<VM> { parametersOf(navigator, route.id) }`.

**Чек-лист нового экрана** (на примере фичи `foo`):
1. Добавить `@Serializable data object/data class` в `FooNavigation.kt`.
2. Добавить вьюмодель `internal class FooViewModel(…, composeNavigator: ComposeNavigator, …) : ComposeBaseViewModel<FooState>(…), FooActions`.
3. Создать `FooActions` рядом с VM (`internal interface FooActions`), UI принимает его, а не VM и не колбэки.
4. Создать `@Composable internal fun FooScreen(viewModel: FooViewModel)` → `FooScreenContent(state, actions = viewModel)`.
5. Зарегистрировать в `FooNavGraphProvider` через `composable<FooNavigation.X, FooViewModel>(viewModelFactory = { koinViewModel { parametersOf(navigator, …) } })`.
6. Добавить `viewModelOf(::FooViewModel)` в `FooModule`; при новых зависимостях — `singleOf`.
7. Если фича новая: `include(":feature:foo")`, Koin-модуль в `RowingApp`/`MainViewController`, NavGraph в `App.navigationBuilder`, зависимость в `shared/build.gradle.kts`.
8. Строки — в `feature/foo/src/commonMain/composeResources/values/strings.xml`.

---

## 5. Экраны: VM / state / actions / UI

Все VM наследуют `ComposeBaseViewModel<State>`; состояние меняется только через `changeState { copy(...) }` (Compose `mutableStateOf`).
UI читает `viewModel.state` напрямую (это `@Composable` state), поэтому коллекции во Flow собираются во VM.

| Экран | VM | State | Actions | UI |
|---|---|---|---|---|
| Дашборд | `shared/.../main/presentation/MainViewModel.kt` | `MainUiState.kt` | `MainScreenActions.kt` | `MainScreen.kt` |
| Список тренировок | `feature/training/.../presentation/list/ListTrainingViewModel.kt` | `list/models/ListTrainingState.kt` | `list/compose/ListTrainingScreenActions.kt` | `list/compose/ListTrainingScreen.kt` |
| Добавление тренировки | `.../presentation/add/AddTrainingViewModel.kt` | `add/AddTrainingState.kt` | методы VM (UI в том же пакете) | `add/AddTrainingScreen.kt` |
| Деталь тренировки | `.../presentation/detail/TrainingDetailViewModel.kt` | `detail/models/TrainingDetailState.kt`, `TrainingDetailPiece.kt` | `detail/compose/TrainingDetailActions.kt` | `detail/compose/*` |
| Сравнение | `.../presentation/comparison/TrainingCompareViewModel.kt` | `comparison/models/TrainingCompareState.kt` | `comparison/TrainingCompareActions.kt` | `comparison/compose/TrainingCompareScreen.kt` |
| График | `.../presentation/chart/TrainingChartViewModel.kt` | `chart/models/TrainingChartState.kt` | `chart/TrainingChartActions.kt` | `chart/compose/TrainingChartScreen.kt` |
| Список атлетов | `feature/athlete/.../presentation/list/AthletesViewModel.kt` | `list/AthletesUiState.kt` | `list/compose/AthletesListActions.kt` | `list/compose/AthletesListScreen.kt` |
| Добавление атлета | `.../presentation/add/AddAthleteViewModel.kt` | `add/AddAthleteState.kt` | `add/compose/AddAthleteScreenActions.kt` | `add/compose/AddAthleteScreen.kt` |
| Деталь атлета | `.../presentation/detail/AthleteDetailViewModel.kt` | `detail/AthleteDetailState.kt` | `detail/AthleteDetailScreenActions.kt` | `detail/compose/AthleteDetailScreen.kt` |
| График веса | `.../presentation/chart/AthleteWeightChartViewModel.kt` | `chart/AthleteWeightChartState.kt` | `chart/AthleteWeightChartActions.kt` | `chart/compose/AthleteWeightChartScreen.kt` |
| Настройки | `feature/settings/.../presentation/SettingsViewModel.kt` | `models/SettingsUiState.kt` | `SettingsActions.kt` | `compose/SettingsScreen.kt` |
| Калькулятор | `feature/calculator/.../presentation/CalculatorViewModel.kt` | `CalculatorState.kt` | — (публичные методы VM) | `compose/CalculatorScreen.kt` |

Ошибки/уведомления в state — `UiText` (`uikit/.../component/UiText.kt`): хранит `StringResource` + аргументы, резолвится `resolve()` в UI.
`*Actions.empty()` используется в `@Preview`.

---

## 6. Данные (Room)

`data/local/src/commonMain/kotlin/by/rowing/sanbaiteam/core/data/local/RowingDatabase.kt` — версия 6, `exportSchema = false`, миграции в companion:
`MIGRATION_1_2` (source-индексы в pieces), `2_3` (saved_training_filter), `3_4` (work display setting), `4_5` (hidden field), `5_6` (вес спортсмена).
Новую схему: поднять `DATABASE_VERSION`, добавить `MIGRATION_N_N+1`, добавить в `MIGRATIONS`, entity — в список `@Database`.

| Таблица | Entity | Ключевые поля |
|---|---|---|
| `athlete_entity` | `athlete/data/entity/AthleteEntity.kt` | `id, name, dateOfBirth, isMale, speedCoachSerial` |
| `athlete_personal_best` | `AthletePersonalBestEntity.kt` | `athleteId, boatType(TrainingPieceType), distanceMeters, timeMillis`; unique (athleteId,boatType,distance) |
| `athlete_weight` | `AthleteWeightEntity.kt` | `athleteId, dateMillis, weightTenths`; PK (athleteId,dateMillis), вес в десятых кг |
| `table_training` | `training/data/entity/TrainingEntity.kt` | `id, dateMillis, type, sourceFileRelativePath` |
| `table_training_athlete_piece` | `TrainingAthletePieceEntity.kt` | `trainingId, athleteId, order, distanceMeters, timeMillis, strokeRate, HR×3, lactate, sourceFileIndex, sourceIntervalIndex` |
| `saved_training_filter` | `SavedTrainingFilterEntity.kt` | `name, payload(JSON TrainingFilter), createdAtMillis` |
| `training_work_display_setting` | `TrainingWorkDisplaySettingEntity.kt` | `pieceType, mode(METERS/MINUTES)` |
| `training_detail_hidden_field` | `TrainingDetailHiddenFieldEntity.kt` | `field` (имя `TrainingDetailField`) |

DAO: `AthleteDao`, `AthletePersonalBestDao`, `AthleteWeightDao`, `TrainingDao` (транзакции сохранения/удаления, запросы pieces), `SavedTrainingFilterDao`, `TrainingWorkDisplaySettingDao`, `TrainingDetailHiddenFieldDao`.
DAO предоставляются Koin в `data/local/.../data/di/DataModule.kt` (`databaseModule` actual: Android/iOS).

Репозитории:
- `AthletesRepository` — интерфейс в `data/local/.../athlete/data/repository/AthletesRepository.kt`, реализация `AthletesRepositoryImpl` в `feature/athlete/.../data/repository/`.
- `TrainingRepository` (`feature/training/.../data/repository/TrainingRepository.kt`, `internal`) — сохранение/удаление/наблюдение списка, деталь, график; **строит presentation-модели** (`ListTrainingStateItem`, `TrainingDetailState`, `TrainingDetailPiece`).
- `SavedTrainingFiltersRepository`, `TrainingDetailSettingsRepository` — там же; `WorkDisplaySettingsRepository` — `feature/settings/.../data/repository/`.
- `TrainingTransferRepository` + `TrainingBackupSerializer` (JSON-бэкап, `FORMAT_VERSION = 2`) — `feature/training/.../data/trainingtransfer/`.
- `AthleteTransferRepository` + `AthleteBackupSerializer` (JSON-бэкап спортсменов, `kind = "sanbai-athletes"`, `FORMAT_VERSION = 1`) — `feature/athlete/.../data/transfer/`; чистое слияние — `AthleteBackupMerge`.
- `AthleteTrainingsCleaner` — интерфейс в `feature/athlete/.../domain/trainings/`, реализация `TrainingAthleteTrainingsCleaner` в `feature/training/.../data/repository/` (удаление связанных тренировок при удалении спортсменов).

---

## 7. Готовое вместо своего (проверять ПЕРЕД тем, как писать)

`core/util/src/commonMain/kotlin/by/rowing/sanbaiteam/core/util/`:

| Задача | Использовать |
|---|---|
| `m:ss.t` из миллисекунд | `RowingTimeFormat.formatDuration(timeMillis)` |
| `m:ss` (без десятых) | `RowingTimeFormat.formatDurationMmSs(timeMillis)` |
| темп /500 м | `RowingTimeFormat.formatPace(timeMillis, distanceMeters)` → `"m:ss.t /500м"`; сырой — `paceMillis(...)` |
| ввод времени: фильтр символов / парсинг | `RowingTimeFormat.sanitizeTimeInput(text)` / `parseTimeInput(text)` (принимает `m:ss.t` и просто секунды) |
| темп гребли (stroke rate) из текста | `RowingTimeFormat.parseStrokeRate(text)` |
| число с одним знаком после точки | `formatTenths(value)`, округление — `Double.roundToTenths()` |
| % от личного рекорда | `PersonalBestPercent.calculate(...)`, `percentFromPaces(recordPace, pace)`, `paceFromPercent(recordPace, percent)`, `format(percent)` (даёт `"—"` для null) |
| даты | `TimeUtils.formatDate(millis)` → `dd.MM.yyyy`; `formatDateWithMonthName` → `dd мар yyyy`; `formatDateDigits`/`parseDateDigits` → `ddMMyyyy`; `startOfDayMillis`, `endOfDayMillis`, `isSameCalendarDay`, `ageInYearsNow` |
| текущее время | `nowInMillis()` (expect/actual, не `System.currentTimeMillis` в common) |

Пустой показатель в UI принято показывать как `"—"`. Константы: `TimeUtils.MILLIS_PER_SECOND`, `MILLIS_PER_TENTH`, `SECONDS_PER_MINUTE`.

Даты тренировок хранятся как «полдень локального дня»: `AddTrainingViewModel.toTrainingDateMillis` (полдень защищает от сдвига дня при таймзонах).

---

## 8. UI-конвенции (коротко; каталог — в [uikit.md](uikit.md))

- Контент внутри `Scaffold`: `Modifier.scaffoldContentInsets(innerPadding)` из `uikit/modifier/ScreenInsets.kt`
  (padding + `consumeWindowInsets` + `imePadding`). Не вешать `imePadding()` на сам `Scaffold`.
- Отступы — только `Spacing.*`, шрифты — `TypographyPalette.*` / `TypographyPaletteSp.*`, цвета — `MaterialTheme.colorScheme` или `ColorPalette.*`, формы — `MaterialTheme.shapes` (Radius 8/12/24).
- Инлайн-действия в карточках/списках — Material3 `TextButton`; `uikit.Button` — только основные CTA (полноширинная, диалоги).
- Клики — через `Modifier.debounceClickable` / `clickableIfProvided` (защита от даблклика; `ClickableState` в Koin).
- Строки — `composeResources/values/strings.xml` соответствующего модуля (не `app/src/main/res`), читаются `stringResource(Res.string.…)`.
  Пакеты `Res`: `shared.resources`, `uikit.resources`, `data.local.resources` (подписи `TrainingPieceType`), `athlete.resources`, `training.resources`, `settings.resources`, `calculator.resources`.
- Превью: `ComposePreviewWrapper { SANBAITeamTheme { … } }` + `@Preview`.

---

## 9. Код-конвенции и detekt

- Всё top-level по умолчанию `internal`; публичное — только с `@AllowDetektPublic` (`core/annotation`). Правило: `TopLevelDeclarationsShouldNotBePublic`.
- Порядок членов класса: свойства/enum-записи/init → конструкторы → public-функции → private-функции → companion. Правило: `WrongClassDeclarationsOrder`.
- Именованные аргументы: если аргументов ≥ 2 и вызов не в одну строку — каждый аргумент на своей строке с запятой. Правило: `MultipleArgumentListWrapping`; то же для параметров функций — `MultipleParameterListWrapping`.
- `MaxLineLength = 130`, запрещены wildcard-imports, `NoUnitReturn`, `ChainWrapping`, `SpacingAround*`. Локально detekt сам исправляет (`autoCorrect` при отсутствии `CI`).
- Прочие активные правила: `LargeClass` (900 строк), `LongMethod` (90), `LongParameterList` (8/22), `NestedBlockDepth` (6), `StringLiteralDuplication`, `DataClassShouldBeImmutable`, `BracesOnIfStatements` (однострочный if — без скобок, многострочный — со скобками).
- Конфиг: `config/detekt.yml`; свои правила: `detekt/src/main/kotlin/com/checker/detekt/rule/`.

---

## 10. Тесты

`kotlin("test")` в `commonTest`. Быстрый поиск: `find . -path "*commonTest*" -name "*Test.kt"`.

| Область | Где |
|---|---|
| Форматтеры/даты | `core/util/src/commonTest/.../RowingTimeFormatTests.kt`, `TimeUtilsTests.kt` |
| График (математика) | `uikit/src/commonTest/.../component/chart/MetricChartMathTest.kt` |
| Настройки объёма | `feature/settings/src/commonTest/.../WorkDisplaySettingsRepositoryTest.kt` |
| Тренировки: репозиторий | `feature/training/src/commonTest/.../data/repository/TrainingRepository*Test.kt` (+ `Fake*Dao`, `TrainingRepositoryTestFactory`) |
| Импорт FIT/CSV | `feature/training/src/commonTest/.../data/trainingimport/*Test.kt` (+ `FitTestSupport`, `FakeTrainingImportStorage`) |
| Графики/сплиты/сравнение | `.../data/chart/`, `.../data/comparison/`, `.../presentation/chart/models/` |
| Фильтры | `.../data/filter/SavedTrainingFilterSerializerTest.kt`, `.../presentation/filter/*Test.kt` |
| Бэкап/перенос | `.../data/trainingtransfer/*Test.kt`, `.../data/serializer/TrainingBackupSerializerTest.kt` |
| Деталь (формат сравнения) | `.../presentation/detail/compose/ComparisonFormatTest.kt` |
| Detekt-правила | `detekt/src/test/kotlin/com/checker/detekt/rule/*Test.kt` |
| PB-проценты | `app/src/test/java/by/rowing/sanbaiteam/core/util/PersonalBestPercentTest.kt` |

---

## 11. Грабли

- **Не запускать сборку/тесты.** Только правки кода; проверку делает пользователь.
- Строки лежат в `composeResources/values/strings.xml`, а не в `res/values` (в `app/src/main/res` — только `app_name`, тема и манифест).
- `TimeUtils` намеренно не использует `Instant.Companion` (проблема резолва на iOS) — не «оптимизировать» на `Instant`.
- Навигатор не эмитит события, пока UI не `attachUi()`; после обработки `App.kt` вызывает `resetCachedNavigation()` — не убирать, иначе белый экран/повтор экрана.
- Back на последнем экране не выталкивает стек (иначе пустой NavHost).
- `TrainingRepository` лежит в data-слое, но возвращает presentation-модели — при изменении моделей списка/детали править и его.
- `TrainingPieceType` — enum с `StringResource`; подписи в `data/local` resources. `isWater` влияет на дефолтный режим объёма (вода — метры, эргометры — минуты).
- `trainingId`/`athleteId` в маршрутах — `Long`; `order` отрезка в UI 1-based, в БД `order` 0-based (в `TrainingRepository.getTrainingChart` сравнение `order - 1`).
- `TrainingImportContainer` (`.fitpack`) пакует несколько файлов; `sourceFileIndex` — индекс файла внутри контейнера, `sourceIntervalIndex` — индекс интервала.
- Ресурсные классы `shared` и `settings` — `publicResClass = true`, остальные internal: из другого модуля их `Res` не видно.
- `AddTrainingScreen`/`AddTrainingViewModel` и `CalculatorScreen`/`CalculatorViewModel` — легаси без `*Actions`-интерфейса (UI принимает VM). При правке этих экранов выносить интерфейс по AGENTS.md, новые экраны — только через `*Actions`.
- `collectAsState` в UI не использовать для стейта VM — VM уже держит Compose-state.

---

## 12. Полезные команды (без Gradle)

```bash
rg -n "class .*ViewModel" --glob '!**/build/**'
find . -path '*commonTest*' -name '*Test.kt'
rg -n "stringResource\(Res\.string" feature/training/src/commonMain | head
rg -n "scaffoldContentInsets|imePadding" --glob '!**/build/**'
```
