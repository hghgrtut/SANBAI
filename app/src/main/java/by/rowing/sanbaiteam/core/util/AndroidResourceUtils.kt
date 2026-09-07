package by.rowing.sanbaiteam.core.util

import android.content.Context
import androidx.annotation.StringRes

class AndroidResourceUtils(
    private val context: Context,
) {

    fun getString(
        @StringRes id: Int,
        vararg args: Any,
    ): String {
        return try {
            if (args.isEmpty()) {
                context.resources?.getString(id)
            } else {
                context.resources?.getString(id, *args)
            }
        } catch (_: Exception) {
            null
        }.orEmpty()
    }
}
