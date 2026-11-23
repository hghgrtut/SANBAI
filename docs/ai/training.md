# feature:training — домен тренировок (карта)

Самый большой модуль. Базовый путь: `feature/training/src/commonMain/kotlin/by/rowing/sanbaiteam/training/`.
Платформенные куски — в `feature/training/src/androidMain/...` и `src/iosMain/...` (те же пакеты).

---

## 1. Файлы по слоям

### Presentation
| Пакет / файл | Что |
|---|---|
| `presentation/list/ListTrainingViewModel.kt` | Список: наблюдение, выделение/удаление, экспорт/импорт бэкапа, фильтры |
| `presentation/list/models/ListTrainingState.kt`, `ListTrainingStateItem.kt`, `ListTrainingPieceItem.kt` | Состояние списка и элементы (элемент реализует `FilterableTraining`) |
| `presentation/list/compose/ListTrainingScreen.kt`, `ListTrainingScreenActions.kt` | UI списка: топ-бар с выбором, фильтры, FAB, диалоги |
| `presentation/add/AddTrainingViewModel.kt` (573 стр.) | Ручной ввод + импорт файлов + сохранение |
| `presentation/add/AddTrainingState.kt`, `AddTrainingValidation.kt`, `AddTrainingScrollTarget.kt` | Черновики отрезков/экипажа, валидация (`validateAddTraining`), адреса автоскролла к ошибке |
| `presentation/add/AddTrainingScreen.kt` (717 стр.) | UI добавления (дата, тип, экипаж, отрезки, импорт) |
| `presentation/detail/TrainingDetailViewModel.kt` | Загрузка детали, сравнение, сплиты, скрытые поля |
| `presentation/detail/models/TrainingDetailState.kt`, `TrainingDetailPiece.kt` | Состояние/модель отрезка (готовые строки форматирования + `chartData` + `comparison`) |
| `presentation/detail/compose/TrainingDetailScreen.kt`, `TrainingPieceCard.kt`, `TrainingSplitTable.kt`, `TrainingDetailSettingsDialog.kt`, `ComparisonFormat.kt`, `TrainingDetailActions.kt` | UI детали и форматирование дельт (`formatSignedInt/Tenths/Seconds`) |
| `presentation/comparison/*` | Экран выбора тренировок для сравнения (`TrainingCompareScreen`, `TrainingCompareViewModel`) и `TrainingComparisonSelectionStore` (сессионный стейт) |
| `presentation/chart/*` | Экран графика отрезка (`TrainingChartViewModel`, `TrainingChartState`, `TrainingChartValueBounds`) |
| `presentation/filter/*` | Движок фильтров: `TrainingFilter`, `TrainingFilterParameter`, `TrainingFilterMatcher`, `TrainingFilterTimeParser`, `TrainingFiltersController`, `FilterableTraining`, `models/*`, `compose/*` (панель, чипы, диалоги) |
| `presentation/platform/TrainingPlatformComposables.kt` | `expect`-пикеры: `rememberTrainingImportPicker`, `rememberTrainingDatePicker` (+ actual в android/ios); файловые диалоги — из `core:file` |
| `presentation/navigation/*` | `TrainingNavigation`, `TrainingNavGraphProvider` |

### Data
| Файл | Что |
|---|---|
| `data/repository/TrainingRepository.kt` | Сохранение/удаление, наблюдение списка, деталь, данные графика; **маппинг в presentation-модели** |
| `data/repository/SavedTrainingFiltersRepository.kt` | CRUD быстрых фильтров (payload = JSON) |
| `data/repository/TrainingDetailSettingsRepository.kt` | Скрытые показатели детали (в БД — только скрытые) |
| `data/chart/TrainingChartReader.kt` | Чтение точек графика из сохранённого файла (FIT/RP3) |
| `data/chart/TrainingSplitCalculator.kt` | Расчёт сплитов по дистанции/времени из точек графика |
| `data/comparison/TrainingComparisonCalculator.kt` | Сравнение базового отрезка со средним по выбранным тренировкам |
| `data/dashboard/TrainingDashboard.kt` | Дашборд: счётчик тренировок + 4 последних (`RECENT_TRAININGS_LIMIT = 4`) |
| `data/filter/SavedTrainingFilter.kt`, `SavedTrainingFilterSerializer.kt` | Сериализация фильтра для быстрых фильтров |
| `data/models/...` | `FitLap/FitRecord/FitWorkout`, `TrainingChart*`, `Comparison*`, `TrainingDetailField`, `TrainingTransferResult` |
| `data/trainingimport/*` | Парсеры FIT/CSV, FIT-декодер, контейнер `.fitpack`, `CsvWriter`, интерфейсы storage/reader |
| `data/trainingtransfer/*` | JSON-бэкап (`TrainingBackupSerializer`), импорт/экспорт (`TrainingTransferRepository`), матчинг атлетов |
| `di/TrainingModule.kt` | Koin: репозитории + все VM (`AddTrainingViewModel` — вручную из-за `importUri`) |

---

## 2. Поток добавления тренировки

1. `AddTrainingScreen` создаёт пикер: `rememberTrainingImportPicker(onFilesPicked = actions::importTrainingBatch)`.
2. `AddTrainingViewModel.importTrainingBatch(handles)`:
   - `TrainingFileReader.readFiles(handles)` → `List<PickedImportFile(bytes, fileName)>`;
   - `TrainingImportParsers.detect(bytes)` определяет парсер по содержимому (контейнер распаковывается, берётся первый файл);
   - все файлы должны быть одного источника, иначе `add_training_import_mixed_sources`;
   - каждый файл распаковывается `TrainingImportContainer.unpackOrSingle(bytes)`, парсится `parser.parse(bytes, fileName)`;
   - тренировки не смешивают типы (`TrainingPieceType`);
   - `applyParsedImport(...)` добавляет отрезки с `sourceFileIndex` (порядковый номер файла в будущем контейнере) и `sourceIntervalIndex` (индекс интервала в файле), подставляет дату (полдень) и атлета по `deviceSerial` / единственному в каталоге.
3. `saveTraining()` → валидация → `TrainingRepository.saveTraining(dateMillis, type, pieces)`.
4. Если были импортированные файлы: `TrainingImportContainer.pack(pendingRawFiles)` → `TrainingImportStorage.saveImportFile(source, bytes, extension)` → `relativePath` пишется в `TrainingEntity.sourceFileRelativePath`.
   Позже по этому пути `TrainingChartReader` перечитывает исходник для графиков.

Источники (`TrainingImportSource`, папка хранения): `SPEED_COACH("speedcoach_imports")`, `CONCEPT2_FIT("concept2_imports")`, `RP3_CSV("rp3_imports")`.

### Парсеры (`data/trainingimport/`)
| Парсер | Формат | Правила |
|---|---|---|
| `Concept2FitParser` | `.fit` PM5 (RowErg/BikeErg) | Только активные лапы — отрезки; отдых даёт `recoveryHeartRate` (отступ 90 c); без отдыха вся работа — один отрезок (`Concept2PieceRanges.concept2PieceRanges()`); HR/SR из `record` |
| `SpeedCoachFitParser` | `.fit` SpeedCoach | Один лап = один отрезок; `total_timer_time` сессии ненадёжен, берутся значения лапов; `cycle_length` не используется; HR/SR пересчитываются из `record`; тип — `SINGLE` |
| `Rp3CsvParser` | `.csv` RP3 (по одному гребку на строку) | Группировка по `workout_interval_id`, дистанция кумулятивная и перезапускается на интервале; дата из имени файла `^\d{12}`; исходник сохраняется как есть |
| `TrainingImportParsers` | — | `detect(bytes)`, `bySource(source)`; порядок: Concept2 → SpeedCoach → RP3 |

FIT-декодер: `FitDecoderImpl.kt` (`ByteReader`, `FileHeader`, разбор definition/data messages) + константы в `FitDecoderCommon.kt`; `FitWorkoutReader.decode(bytes)` → `FitWorkout(isRowing, sport, subSport, manufacturer, deviceSerial, startTimeMillis, laps, records)`.

Контейнер `.fitpack` (`TrainingImportContainer`): magic `SANBAIFIT`, version 1, заголовок 12 байт, затем `[uint32 length][bytes]` × N. `pack`, `unpack`, `unpackOrSingle`, `isContainer`.

---

## 3. Деталь тренировки

`TrainingRepository.getTrainingDetail(trainingId, comparedTrainingIds)`:
- берёт `TrainingEntity`, pieces, имена атлетов, PB (только `PersonalBestPercent.DEFAULT_PB_DISTANCE_METERS = 2000` и `boatType = training.type`);
- PB-процент отрезка — среднее по экипажу (`PersonalBestPercent.calculate` + `roundToTenths`);
- `chartData` — перечитывается из исходного файла по `sourceFileIndex/sourceIntervalIndex` (`TrainingChartReader.read`);
- `comparison` — `TrainingComparisonCalculator.compare(base, compared)` по тому же `order`;
- строки форматируются здесь: `RowingTimeFormat.formatDuration/formatPace`, `formatTenths`, `PersonalBestPercent.format`.

Состояние `TrainingDetailState`: `pieces`, `comparedTrainingIds`, `expandedComparisonOrders`, `splitLength`, `splitsByOrder`, `hiddenFields`, `showSettingsDialog`.
`TrainingDetailViewModel` подписан на `TrainingDetailSettingsRepository.hiddenFields` и на `TrainingComparisonSelectionStore.selection` (`distinctUntilChanged` + `collectLatest`), поэтому после экрана сравнения деталь перезагружается сама.

Скрываемые показатели — `data/models/detail/TrainingDetailField.kt` (9 значений: DISTANCE, TIME, PACE, STROKE_RATE, AVG/MAX/RECOVERY_HEART_RATE, LACTATE, PERCENT_OF_PB). Последний видимый скрыть нельзя — `Set<TrainingDetailField>.canToggle(field)`.

Сплиты: `TrainingSplitCalculator.calculate(chart: TrainingPieceChartData, totalDistanceMeters, totalTimeMillis, length: TrainingSplitLength, paceDistanceMeters)` → `List<TrainingPieceSplit>?`.
`TrainingSplitLength.Distance(meters)` / `.Time(millis)`; дистанция/время нормализуются (`scale`), значения SR/HR берутся из точек в диапазоне.

---

## 4. Графики

`TrainingChartReader.read(sourceFileIndex, sourceFileBytes, sourceIntervalIndex, pieceType)` → `TrainingPieceChartData?`.
Для FIT: объединяет нужные лапы, считает elapsed, дистанцию, SR (`record.cadence`), HR (`record.heartRate`); «speed»-серия на самом деле темп (время на дистанцию):
- гребля — на 500 м, BikeErg и WattBike — на 1000 м (`TrainingPieceType.paceDistanceMeters` в `data/local/.../TrainingPieceType.kt` — единый источник для графика, деталей, сравнения и сплитов);
- отбрасываются значения вне `paceRange` (MIN/MAX секунд).
Для RP3: точки из строк, темп из `estimated_500m_time` или расчётный.

`TrainingPieceChartData(durationMillis, speed, strokeRate, heartRate, distance)`:
`availableMetrics` (SPEED/SR/HR) и `points(metric)`.
`TrainingChartMetric { SPEED, STROKE_RATE, HEART_RATE }` — иконки + строки.
`TrainingChartResult`: `Success(data, pieceType)`, `PieceNotFound`, `SourceUnavailable`.
Запрос: `TrainingRepository.getTrainingChart(trainingId, order)` — `order` 1-based, в БД 0-based.
UI: `TrainingChartScreen` использует `MetricLineChart` (см. [uikit.md](uikit.md)), форматирует значения через `RowingTimeFormat.formatDuration`/`formatDurationMmSs`, границы шкалы — `TrainingChartValueBounds` (`presentation/chart/models/`).

---

## 5. Сравнение

- `TrainingComparisonSelectionStore` (Koin single) — `selection: StateFlow<Set<Long>?>`; выбирается одна база (экран детали открыт) + набор сравниваемых.
- `TrainingCompareViewModel` грузит кандидатов того же типа (`TrainingRepository.getComparisonCandidates`), применяет те же фильтры, `onConfirm()` пишет выбор в store и делает `navigateBack()`.
- `TrainingComparisonCalculator.compare(base: ComparablePiece, compared: List<ComparablePiece>)` → `PieceComparison(comparedTrainingsCount, metrics)`.
  `ComparisonMetric`: DISTANCE, TIME, PACE, STROKE_RATE, AVG/MAX/RECOVERY_HEART_RATE, LACTATE, PERCENT_OF_PB.
  Для каждого показателя — `MetricComparison` (база, среднее, дельта, тренд `ComparisonTrend`), проценты считаются по среднему.
- UI: `TrainingDetailPiece.comparison` → раскрывающиеся блоки в `TrainingPieceCard`/`TrainingSplitTable`; дельты — `ComparisonFormat.kt` (`formatSignedInt/Tenths/Seconds`).

---

## 6. Фильтры

`TrainingFilter` (AND между параметрами, OR внутри значения):
`athleteIds`, `trainingTypes`, `pieceTimeRange: LongRange?`, `pieceDistanceRange: IntRange?`, `lactateOnly`, `dateRange: LongRange?`.
Хелперы: `isEmpty`, `activeParametersCount`, `isActive(parameter)`, `without(parameter)`, `applyPreset(preset)` (быстрый фильтр накладывается поверх текущего).

`TrainingFilterParameter`: ATHLETES, TRAINING_TYPE, PIECE_TIME, PIECE_DISTANCE, LACTATE, DATE.

- `TrainingFilterMatcher.matches(filter, FilterableTraining)` + `List<T>.applyTrainingFilter(filter)` — применяется в UI (`ListTrainingScreen`, `TrainingCompareViewModel`) через `remember`.
- `TrainingFilterTimeParser.parseMillis` — только `m:ss` или минуты (десятые не поддерживаются; это НЕ `RowingTimeFormat`).
- `TrainingFiltersController` — общая логика для списка и сравнения: открытие/закрытие шита, быстрые фильтры (создание/переименование/удаление/перезапись), каталог атлетов.
- `TrainingFiltersState` (`presentation/filter/models/`) — общее состояние панели; персистятся только быстрые фильтры (`saved_training_filter`, JSON через `SavedTrainingFilterSerializer`).
- UI панели: `TrainingFiltersPanel.kt` (598 стр.), `TrainingQuickFilters.kt`, `TrainingFilterActionButton.kt`, `TrainingFiltersActions.kt`.

---

## 7. Объём работы в списке

`TrainingWorkDisplayMode { METERS, MINUTES }`, per `TrainingPieceType` (настройки — `feature/settings`).
Дефолт: вода → метры, эргометры → минуты (`WorkDisplaySettingsRepository.modes`).
Формирование строки — `TrainingRepository.formatWorkSummary` (группы одинаковых значений: `"4×500м"`, `"3×2 мин"`, разделитель `" + "`).
Минуты округляются до секунды; ровные минуты выводятся как `"N мин"`, иначе `m:ss`.

---

## 8. Бэкап / перенос

`TrainingBackupSerializer` (JSON, `ignoreUnknownKeys`, `explicitNulls = false`, `FORMAT_VERSION = 2`):
`TrainingBackup(formatVersion, athletes: [name, dateOfBirthMillis, isMale], trainings: [dateMillis, type, source?, sourceFileBase64?, sourceFileExtension?, pieces])`.
`TrainingTransferRepository.exportSelectedBackup(ids)` / `importBackup(backup)` → `TrainingTransferResult(trainingsImported, athletesCreated)`; исходные файлы кодируются base64 и восстанавливаются в storage.
Атлеты сопоставляются `TrainingTransferAthleteMatcher.matchBackupAthletesToExisting` (совпадение по данным, не по id).
UI: экспорт/импорт в `ListTrainingScreen` через `rememberCreateTextFileLauncher` / `rememberOpenTextFileLauncher` из `core:file`; запись/чтение — `TextFileAccess`; уведомления — `UiText`.

---

## 9. Строки и ресурсы

`feature/training/src/commonMain/composeResources/values/strings.xml` (~220 строк), пакет `by.rowing.sanbaiteam.training.resources`.
Префиксы: `training_*` (список/фильтры/деталь/график/сравнение), `add_training_*` (форма и импорт), `training_transfer_*` (бэкап), `training_quick_filter_*`.
Подписи типов тренировок лежат в `data/local/src/commonMain/composeResources/values/strings.xml` (`TrainingPieceType.uiResId`).

---

## 10. Типовые задачи

| Задача | Куда идти |
|---|---|
| Добавить показатель отрезка | `TrainingDetailField` (enum) → `TrainingAthletePieceEntity` (+миграция) → `TrainingDetailPiece` → `TrainingRepository.getTrainingDetail` → `TrainingPieceCard`/`TrainingDetailSettingsDialog` |
| Изменить формат времени/темпа в детали | `TrainingRepository.buildDetailPieces` (строки) или `RowingTimeFormat` (общий формат) |
| Новый формат импорта | `TrainingImportSource` + парсер (`TrainingImportParser`) → `TrainingImportParsers` → при необходимости `TrainingChartReader` (для графиков) |
| Новый фильтр | `TrainingFilter` + `TrainingFilterParameter` (без параметра не активируется) → `TrainingFilterMatcher` → `TrainingFiltersPanel`/шитья → `TrainingFilterTimeParser`/сериализатор |
| Новый экран в тренировках | `TrainingNavigation` → `TrainingNavGraphProvider` → `TrainingModule` → см. чек-лист в [PROJECT_MAP.md](PROJECT_MAP.md) |
| Изменить расчёт сравнения | `TrainingComparisonCalculator` (+ `ComparisonMetric`, `TrainingComparisonCalculatorTest`) |
| Изменить сплиты | `TrainingSplitCalculator` (+ `TrainingSplitCalculatorTest`) |
| Дашборд | `TrainingDashboardImpl` (`data/dashboard/TrainingDashboard.kt`) |

Тесты: `feature/training/src/commonTest/kotlin/by/rowing/sanbaiteam/training/` — `data/repository/*Test.kt` (+ фейки DAO и `TrainingRepositoryTestFactory`), `data/trainingimport/*Test.kt` (+ `FitTestSupport`), `data/chart/`, `data/comparison/`, `data/trainingtransfer/`, `presentation/filter/`, `presentation/detail/compose/ComparisonFormatTest.kt`, `presentation/chart/models/TrainingChartValueBoundsTest.kt`, `presentation/add/AddTrainingValidationTest.kt`.
