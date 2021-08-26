package cc.cryptopunks.astral.service.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal fun logcatFlow(): Flow<String> = flow {
    Runtime.getRuntime().exec("logcat -c") // clear
    Runtime.getRuntime().exec("logcat")
        .inputStream
        .bufferedReader()
        .useLines { lines -> lines.forEach { line -> emit(line) } }
}

const val ASTRAL = "Astral"
const val GO_LOG = "GoLog"

internal fun Flow<String>.formatAstralLogs(): Flow<String> =
    filter { ASTRAL in it || GO_LOG in it }.map { line ->
        line.split(' ', limit = 5)
            .run { get(1) + " " + get(4) + "\n\n" }
            .split(": ", limit = 2)
            .run { get(0) + ":\n" + get(1) }
    }

