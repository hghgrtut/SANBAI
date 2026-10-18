package by.rowing.sanbaiteam.uikit.component

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity

@Composable
internal actual fun defaultBackAction(): () -> Unit {
    val activity = LocalActivity.current as? FragmentActivity
    return { activity?.onBackPressedDispatcher?.onBackPressed() }
}
