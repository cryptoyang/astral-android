package cc.cryptopunks.astral.wrapdrive.util

import android.content.ClipData

fun ClipData.items(): List<ClipData.Item> = List(itemCount, this::getItemAt)
