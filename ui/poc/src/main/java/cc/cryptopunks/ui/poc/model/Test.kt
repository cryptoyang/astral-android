package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.mapper.openrpc.splitPath
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument


fun main() {
    println(false)
}

private fun legacy() {
    "hgfh.sad[1].asd".isPath()
        .let(::println)

    "hgfh.sad[1].asd".splitPath()
        .let(::println)

    "hgfh.sad.1.asd".splitPath()
        .let(::println)

    val doc = MessengerApi.generateOpenRpcDocument()

    Jackson.prettyWriter.writeValueAsString(doc).let(::println)

    println()
    println()

//    val context = UI.Context(doc)

//    Jackson.prettyWriter.writeValueAsString(context.model).let(::println)

    println()
    println()

//    val resolvers = context.model.resolvers()

//    resolvers.let(::println)
}
