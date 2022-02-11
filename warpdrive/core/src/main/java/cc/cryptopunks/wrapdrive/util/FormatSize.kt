package cc.cryptopunks.wrapdrive.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

private val units = arrayOf("B", "KB", "MB", "GB", "TB")
private const val unit = 1000.0


fun Long.formatSize(): String =
    if (this <= 0) "0" + units[0]
    else (log10(toDouble()) / log10(unit)).toInt().let { digitGroup ->
        DecimalFormat("#,##0.#")
            .format(this / unit.pow(digitGroup.toDouble()))
            .toString() + units[digitGroup]
    }
