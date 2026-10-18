package by.rowing.sanbaiteam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import by.rowing.sanbaiteam.shared.App

class MainActivity : ComponentActivity() {

    private var pendingImportUri by mutableStateOf<String?>(null)

    private val mainActivityContent: @Composable () -> Unit = {
        App(importUri = pendingImportUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingImportUri = extractImportUri(intent)
        enableEdgeToEdge()
        setContentView(
            ComposeView(this).apply {
                setViewCompositionStrategy(strategy = ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent(mainActivityContent)
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingImportUri = extractImportUri(intent)
    }

    private fun extractImportUri(intent: Intent?): String? = when (intent?.action) {
        Intent.ACTION_VIEW -> intent.data?.toString()
        Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.toString()
        else -> null
    }
}
