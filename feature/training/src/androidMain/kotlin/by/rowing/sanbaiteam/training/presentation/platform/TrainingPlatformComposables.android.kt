package by.rowing.sanbaiteam.training.presentation.platform

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

private const val JSON_APP_MIME_TYPE = "application/json"
private const val JSON_TEXT_MIME_TYPE = "text/json"
private const val TEXT_PLAIN_MIME_TYPE = "text/plain"
private const val ANY_MIME_TYPE = "*/*"

@Composable
internal actual fun rememberTrainingImportPicker(onFilesPicked: (List<String>) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> onFilesPicked(uris.map { it.toString() }) }
    return remember(launcher) { { launcher.launch(ANY_MIME_TYPE) } }
}

@Composable
internal actual fun rememberCreateBackupLauncher(onCreated: (String) -> Unit): (String) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = JSON_APP_MIME_TYPE)
    ) { uri -> if (uri != null) onCreated(uri.toString()) }
    return remember(launcher) { { fileName -> launcher.launch(fileName) } }
}

@Composable
internal actual fun rememberOpenBackupLauncher(onOpened: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onOpened(uri.toString()) }
    return remember(launcher) {
        { launcher.launch(arrayOf(JSON_APP_MIME_TYPE, JSON_TEXT_MIME_TYPE, TEXT_PLAIN_MIME_TYPE)) }
    }
}

@Composable
internal actual fun rememberTrainingDatePicker(
    initialDateMillis: Long,
    onDatePicked: (year: Int, month: Int, dayOfMonth: Int) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val calendar = remember(initialDateMillis) {
        Calendar.getInstance().apply { timeInMillis = initialDateMillis }
    }
    return remember(context, calendar) {
        {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth -> onDatePicked(year, month + 1, dayOfMonth) },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
            ).show()
        }
    }
}
