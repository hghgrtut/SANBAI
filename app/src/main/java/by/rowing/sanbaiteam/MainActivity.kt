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
        // Файл из Intent отдаём в UI только при первом создании Activity. При пересоздании
        // (поворот экрана, возврат из фона, смена конфигурации) Intent остаётся тем же, и
        // повторная навигация открывала бы второй экран импорта поверх восстановленного стека
        // с повторным разбором того же файла.
        pendingImportUri = if (savedInstanceState == null) extractImportUri(intent) else null
        enableEdgeToEdge()
        setContentView(
            ComposeView(this).apply {
                // ID нужен, чтобы Android сохранил и восстановил состояние Compose
                // (навигационный стек и rememberSaveable) при пересоздании Activity.
                id = android.R.id.content
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