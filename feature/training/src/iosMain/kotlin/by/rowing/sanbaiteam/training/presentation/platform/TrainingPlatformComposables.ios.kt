package by.rowing.sanbaiteam.training.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
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
@Composable
internal actual fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit {
    val presenter = rememberDocumentPickerPresenter(
        onFilesPicked = onFilesPicked,
        contentTypes = listOf(utType(UT_TYPE_DATA)),
        allowsMultipleSelection = true,
    )
    return remember {
        { presenter() }
    }
}

/**
 * Возвращает действие, открывающее Share Sheet для сохранения резервной копии.
 * Принимает предлагаемое имя файла; путь для записи бэкапа передаётся в [onCreated]
 * до показа Share Sheet, чтобы файл успел записаться, пока пользователь выбирает назначение.
 */
@Composable
internal actual fun rememberCreateBackupLauncher(onCreated: (String) -> Unit): (String) -> Unit {
    val currentOnCreated by rememberUpdatedState(onCreated)
    return remember {
        { fileName: String ->
            topMostViewController()?.let { root ->
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
@Composable
internal actual fun rememberOpenBackupLauncher(onOpened: (String) -> Unit): () -> Unit {
    val presenter = rememberDocumentPickerPresenter(
        onFilesPicked = { paths -> paths.firstOrNull()?.let { path -> onOpened(path) } },
        contentTypes = listOf(utType(UT_TYPE_JSON), utType(UT_TYPE_TEXT)),
        allowsMultipleSelection = false,
    )
    return remember {
        { presenter() }
    }
}

private fun utType(identifier: String): UTType =
    UTType.typeWithIdentifier(identifier) ?: error("Unknown UTType: $identifier")

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
            topMostViewController()?.let { root ->
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
                root.presentViewController(
                    documentPicker,
                    animated = true,
                    completion = null
                )
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
        presentShareSheet(
            root = root,
            tempPath = tempPath
        )
    } else if (attemptsLeft > 0) {
        NSTimer.scheduledTimerWithTimeInterval(
            interval = SHARE_WAIT_INTERVAL_SECONDS,
            repeats = false,
        ) { timer ->
            timer?.invalidate()
            presentShareSheetWhenReady(
                root = root,
                tempPath = tempPath,
                attemptsLeft = attemptsLeft - 1
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentShareSheet(
    root: UIViewController,
    tempPath: String
) {
    val shareSheet = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(tempPath)),
        applicationActivities = null,
    )
    shareSheet.completionWithItemsHandler = { _, _, _, _ ->
        NSFileManager.defaultManager.removeItemAtPath(
            tempPath,
            error = null
        )
    }
    root.presentViewController(
        shareSheet,
        animated = true,
        completion = null
    )
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