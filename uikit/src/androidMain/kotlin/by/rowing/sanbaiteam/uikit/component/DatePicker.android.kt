package by.rowing.sanbaiteam.uikit.component

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import java.util.Calendar

@AllowDetektPublic
@Composable
actual fun rememberDatePicker(
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