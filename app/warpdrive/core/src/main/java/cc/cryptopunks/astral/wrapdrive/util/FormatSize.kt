package cc.cryptopunks.astral.wrapdrive.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


private val units = arrayOf("B", "KB", "MB", "GB", "TB")

fun formatSize(size: Long): String =
    if (size <= 0) "0" + units[0]
    else (log10(size.toDouble()) / log10(1024.0)).toInt().let { digitGroup ->
        DecimalFormat("#,##0.#")
            .format(size / 1024.0.pow(digitGroup.toDouble()))
            .toString() + units[digitGroup]
    }
