package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import by.rowing.sanbaiteam.training.resources.Res
import by.rowing.sanbaiteam.training.resources.cancel
import by.rowing.sanbaiteam.training.resources.ok
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSTimer
import platform.Foundation.NSUUID
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject

private const val UT_TYPE_DATA = "public.data"
private const val UT_TYPE_JSON = "public.json"
private const val UT_TYPE_TEXT = "public.plain-text"

/** Сколько раз (с шагом [SHARE_WAIT_INTERVAL_SECONDS]) ждём появления файла бэкапа перед Share Sheet. */
private const val SHARE_WAIT_ATTEMPTS = 40
private const val SHARE_WAIT_INTERVAL_SECONDS = 0.05

/**
 * Возвращает действие, открывающее системный выбор файлов тренировки.
 * Выбранные файлы копируются во временный каталог, пути возвращаются как строки.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit {
    val presenter = rememberDocumentPickerPresenter(
        onFilesPicked = onFilesPicked,
        contentTypes = listOf(utType(UT_TYPE_DATA)),
        allowsMultipleSelection = true,
    )
    return remember { { presenter() } }
}

/**
 * Возвращает действие, открывающее Share Sheet для сохранения резервной копии.
 * Принимает предлагаемое имя файла; путь для записи бэкапа передаётся в [onCreated]
 * до показа Share Sheet, чтобы файл успел записаться, пока пользователь выбирает назначение.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberCreateBackupLauncher(onCreated: (String) -> Unit): (String) -> Unit {
    val currentOnCreated by rememberUpdatedState(onCreated)
    return remember {
        { fileName: String ->
            val root = topMostViewController()
            if (root != null) {
                val tempPath = "${NSTemporaryDirectory()}backup_${NSUUID().UUIDString()}_$fileName"
                currentOnCreated(tempPath)
                presentShareSheetWhenReady(
                    root = root,
                    tempPath = tempPath,
                    attemptsLeft = SHARE_WAIT_ATTEMPTS,
                )
            }
        }
    }
}

/**
 * Возвращает действие, открывающее системный выбор файла резервной копии.
 * Выбранный файл копируется во временный каталог, путь возвращается как строка.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberOpenBackupLauncher(onOpened: (String) -> Unit): () -> Unit {
    val presenter = rememberDocumentPickerPresenter(
        onFilesPicked = { paths -> paths.firstOrNull()?.let { path -> onOpened(path) } },
        contentTypes = listOf(utType(UT_TYPE_JSON), utType(UT_TYPE_TEXT)),
        allowsMultipleSelection = false,
    )
    return remember { { presenter() } }
}

/**
 * Возвращает действие, открывающее выбор даты.
 * Месяц в [onDatePicked] — от 1 до 12.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun rememberTrainingDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit {
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = { openDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                        val components = date.toLocalDateTime(TimeZone.currentSystemDefault())
                        onDatePicked(components.year, components.monthNumber, components.dayOfMonth)
                    }
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(),
            )
        }
    }

    return remember { { openDialog.value = true } }
}

@OptIn(ExperimentalForeignApi::class)
private fun utType(identifier: String): UTType =
    UTType.typeWithIdentifier(identifier) ?: error("Unknown UTType: $identifier")

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun rememberDocumentPickerPresenter(
    onFilesPicked: (List<String>) -> Unit,
    contentTypes: List<UTType>,
    allowsMultipleSelection: Boolean,
): () -> Unit {
    val currentOnFilesPicked by rememberUpdatedState(onFilesPicked)
    val delegateHolder = remember { DocumentPickerDelegateHolder() }
    return remember {
        {
            val root = topMostViewController()
            if (root != null) {
                val documentPicker = UIDocumentPickerViewController(
                    forOpeningContentTypes = contentTypes,
                    asCopy = true,
                )
                documentPicker.allowsMultipleSelection = allowsMultipleSelection
                val delegate = DocumentPickerDelegate(
                    onFinished = { delegateHolder.delegate = null },
                    onPicked = { urls ->
                        currentOnFilesPicked(urls.map { url -> copyToTempDirectory(url) })
                    },
                )
                // UIKit хранит delegate слабой ссылкой — держим сильную до завершения выбора.
                delegateHolder.delegate = delegate
                documentPicker.delegate = delegate
                root.presentViewController(documentPicker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun copyToTempDirectory(url: NSURL): String {
    val tempFile = "${NSTemporaryDirectory()}import_${NSUUID().UUIDString()}_${url.lastPathComponent}"
    val copied = NSFileManager.defaultManager.copyItemAtURL(
        srcURL = url,
        toURL = NSURL.fileURLWithPath(tempFile),
        error = null,
    )
    return if (copied) tempFile else url.path ?: tempFile
}

/**
 * Показывает Share Sheet, когда файл появился на диске.
 * Вьюмодель пишет бэкап асинхронно после [rememberCreateBackupLauncher]'s onCreated,
 * поэтому дожидаемся записи с коротким интервалом и ограниченным числом попыток.
 */
@OptIn(ExperimentalForeignApi::class)
private fun presentShareSheetWhenReady(
    root: UIViewController,
    tempPath: String,
    attemptsLeft: Int,
) {
    if (NSFileManager.defaultManager.fileExistsAtPath(tempPath)) {
        presentShareSheet(root = root, tempPath = tempPath)
    } else if (attemptsLeft > 0) {
        NSTimer.scheduledTimerWithTimeInterval(
            interval = SHARE_WAIT_INTERVAL_SECONDS,
            repeats = false,
        ) { timer ->
            timer?.invalidate()
            presentShareSheetWhenReady(root = root, tempPath = tempPath, attemptsLeft = attemptsLeft - 1)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentShareSheet(root: UIViewController, tempPath: String) {
    val shareSheet = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(tempPath)),
        applicationActivities = null,
    )
    shareSheet.completionWithItemsHandler = { _, _, _, _ ->
        NSFileManager.defaultManager.removeItemAtPath(tempPath, error = null)
    }
    root.presentViewController(shareSheet, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun topMostViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/**
 * UIKit хранит delegate документ-пикера слабой ссылкой,
 * поэтому выбирающий контроллер держит сильную ссылку через [DocumentPickerDelegateHolder].
 */
private class DocumentPickerDelegateHolder {
    var delegate: DocumentPickerDelegate? = null
}

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
    private val onFinished: () -> Unit,
    private val onPicked: (List<NSURL>) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onFinished()
        @Suppress("UNCHECKED_CAST")
        onPicked(didPickDocumentsAtURLs as List<NSURL>)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onFinished()
    }
}
