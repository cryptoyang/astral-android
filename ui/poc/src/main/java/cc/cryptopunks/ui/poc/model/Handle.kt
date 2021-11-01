package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.api.handle
import cc.cryptopunks.ui.poc.mapper.Jackson
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import kotlin.math.absoluteValue

suspend fun Pair<SendChannel<Command.Output>, Flow<Command.Input>>.handleUsing(
    uiContext: UI.Context
) {
    second.handle(uiContext).collect(first::send)
}

fun Flow<Command.Input>.handle(
    uiContext: UI.Context
): Flow<Command.Output> {
    var method: Api.Method? = null
    var text: CharSequence? = null
    var matching: List<Matching> = uiContext.model.defaultMatching()

    return transform { input ->
        suspend fun Command.Output.out() = emit(this)
        when (input) {

            is Command.GetMethods ->
                Command.SelectMethod(matching.calculateScore(text)).out()

            is Command.InputText -> when (method) {
                null -> {
                    val prev = text?.splitChunks()?.filter { it.isNotBlank() } ?: emptyList()
                    val next =
                        input.text?.splitChunks()?.filter { it.isNotBlank() } ?: emptyList()

                    val common = prev.withIndex().takeWhile { (index, value) ->
                        value == next.getOrNull(index)
                    }

                    val additional = next.drop(common.size)

                    if (input.text.isNullOrBlank())
                        matching = uiContext.model.defaultMatching()

                    matching = matching
                        .filter { it.index == common.lastIndex }
                        .accumulateMatchingElements(additional)

                    text = input.text

                    val latestIndex = matching.lastOrNull()?.index ?: -1
                    val latestMatching = matching.filter { it.index == latestIndex }

                    Command.SelectMethod(latestMatching.calculateScore(text)).out()
                }
            }

            is Command.Selected -> {
                method = input.method

                val args = matching
                    .filter { it.element is Matching.Param.Arg }
                    .associate { it.element.name to it.chunk }

                Command.Status(method!!, args).out()

                if (args.size < method!!.params.size) {

                    val (name, type) = method!!.params
                        .minus(args.keys)
                        .toList()
                        .first()

                    Command.ProvideParam(
                        name = name,
                        type = type,
                        resolvers = uiContext.resolvers.getValue(type.hashId)
                    ).out()

                } else {

                    Command.Ready.out()
                }
            }

            is Command.SetParam -> {
                matching = matching + Matching(
                    method = method!!,
                    chunk = input.value,
                    index = matching.last().index,
                    element = Matching.Param.Arg(input.name)
                )


                val args = matching
                    .filter { it.element is Matching.Param.Arg }
                    .associate { it.element.name to it.chunk }

                Command.Status(method!!, args).out()

                if (args.size < method!!.params.size) {

                    val (name, type) = method!!.params
                        .minus(args.keys)
                        .toList()
                        .first()

                    Command.ProvideParam(
                        name = name,
                        type = type,
                        resolvers = uiContext.resolvers.getValue(type.hashId)
                    ).out()

                } else {

                    Command.Ready.out()
                }
            }

            Command.Execute -> {

                val fullName = uiContext.model.id + "$" + method!!.id

                val argsMap = matching
                    .filter { it.element is Matching.Param.Arg }
                    .associate { it.element.name to it.chunk }

                val clazz = Class.forName(fullName) as Class<MessengerApi.Method>

                var json = "{}"

                val rpcMethod: MessengerApi.Method = Jackson.run {
                    when {
                        argsMap.isEmpty() ->
                            jsonMapper.readValue(json, clazz)

                        else -> Jackson.run {

                            json = prettyWriter.writeValueAsString(argsMap)

                            println(json)

                            jsonMapper.readValue(json, clazz)
                        }
                    }
                }

                val result = handle(rpcMethod)

                if (result != Unit) {
                    uiContext.data += UI.Data(
                        source = method!!,
                        args = argsMap,
                        result = result
                    )

                    Command.Update(uiContext).out()
                }
            }
        }
    }
}

private fun CharSequence.splitChunks() = split(" ").filter { it.isNotBlank() }

private fun Api.Model.defaultMatching(): List<Matching> =
    methods.values.map(::Matching)

private fun List<Matching>.accumulateMatchingElements(
    chunks: List<String>,
    index: Int = nextIndex(),
): List<Matching> =
    chunks.foldIndexed(this) { i, acc, chunk ->
        acc.accumulateMatchingElements(chunk, i + index)
    }

private fun List<Matching>.accumulateMatchingElements(
    chunk: String,
    index: Int = nextIndex(),
): List<Matching> = this + this
    .filter { it.index == index - 1 }
    .map { it.method }
    .findMatchingElements(chunk)
    .flatMap { (method, elements) ->
        elements.map { element ->
            Matching(
                method = method,
                chunk = chunk,
                index = index,
                element = element,
            )
        }
    }

private fun Collection<Api.Method>.findMatchingElements(chunk: String): Map<Api.Method, List<Matching.Element>> {

    val elements = mutableListOf<Pair<Matching.Element, Api.Method>>()

    forEach { method ->

        if (method.id.contains(chunk, true))
            elements += Matching.MethodName to method

        method.params.forEach { (name, type) ->
            when {
                name.contains(chunk, true) ->
                    elements += Matching.Param.Name(name) to method
                // TODO type matcher
            }
        }
    }

    return elements.groupBy(
        keySelector = { (_, method) -> method },
        valueTransform = { (element, _) -> element }
    )
}

private fun List<Matching>.nextIndex() = lastOrNull()?.run { index + 1 } ?: 0

private fun List<Matching>.calculateScore(
    chunks: CharSequence?
): List<Score> =
    groupBy { it.method }.map { (method, matching) ->
        Score(
            method = method,
            matching = matching,
            score = matching.score(chunks?.splitChunks() ?: emptyList())
        )
    }.sortedBy { it.score.absoluteValue }

private fun List<Matching>.score(chunks: List<String>): Int {
    val matchedParams = map { matching ->
        val element = matching.element
        val method = matching.method
        when (element) {
            is Matching.MethodName -> method
            is Matching.Param.Name -> method.params[element.name]!!
            is Matching.Param.Type -> TODO()
            is Matching.Param.Arg -> TODO()
        }
    }

    val matchedChunks = map { it.chunk }.toSet()
    val notMatchedChunks = chunks.filter { chunk -> chunk in matchedChunks }

    return matchedParams.size - (first().method.params.size + 1) - notMatchedChunks.size
}
