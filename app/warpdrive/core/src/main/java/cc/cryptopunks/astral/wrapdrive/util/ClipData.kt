package cc.cryptopunks.astral.wrapdrive.util

import android.content.ClipData

fun ClipData.items() = List(itemCount, this::getItemAt)
