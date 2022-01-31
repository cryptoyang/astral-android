package cc.cryptopunks.astral.wrapdrive.util

import android.app.Activity
import android.widget.Toast

fun Activity.toast(e: Throwable) {
    Toast.makeText(
        this,
        e.localizedMessage,
        Toast.LENGTH_SHORT
    ).show()
}
