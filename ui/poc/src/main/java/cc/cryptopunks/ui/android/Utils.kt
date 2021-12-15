package cc.cryptopunks.ui.android

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.core.widget.doOnTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import splitties.views.imageDrawable

operator fun <T : View> View.invoke(@IdRes id: Int): T =
    findViewById(id)

fun ComponentActivity.backEventsFlow(): Flow<Unit> = callbackFlow {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            trySend(Unit)
        }
    }
    onBackPressedDispatcher.addCallback(onBackPressedCallback)
    awaitClose {
        onBackPressedCallback.remove()
    }
}

fun EditText.textChangesFlow(): Flow<String?> = callbackFlow {
    val watcher = doOnTextChanged { text, _, _, _ ->
        trySend(text?.toString())
    }
    awaitClose {
        removeTextChangedListener(watcher)
    }
}

fun FloatingActionButton.setIcon(drawable: Drawable? = null) {
    when (drawable) {
        is ShapeTextDrawable -> {
            foreground = drawable
            imageDrawable = null
        }
        else -> {
            foreground = null
            imageDrawable = drawable
        }
    }
}
