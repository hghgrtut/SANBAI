package by.rowing.sanbaiteam.core.file

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic

private const val JSON_APP_MIME_TYPE = "application/json"
private const val JSON_TEXT_MIME_TYPE = "text/json"
private const val TEXT_PLAIN_MIME_TYPE = "text/plain"
private const val ANY_MIME_TYPE = "*/*"

@AllowDetektPublic
@Composable
actual fun rememberCreateTextFileLauncher(onCreated: (String) -> Unit): (String) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = JSON_APP_MIME_TYPE)
    ) { uri -> if (uri != null) onCreated(uri.toString()) }
    return remember(launcher) {
        { fileName -> launcher.launch(fileName) }
    }
}

@AllowDetektPublic
@Composable
actual fun rememberOpenTextFileLauncher(onOpened: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onOpened(uri.toString()) }
    return remember(launcher) {
        { launcher.launch(arrayOf(JSON_APP_MIME_TYPE, JSON_TEXT_MIME_TYPE, TEXT_PLAIN_MIME_TYPE)) }
    }
}

@AllowDetektPublic
@Composable
actual fun rememberOpenFilesLauncher(onFilesPicked: (List<String>) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> onFilesPicked(uris.map { it.toString() }) }
    return remember(launcher) {
        { launcher.launch(ANY_MIME_TYPE) }
    }
}