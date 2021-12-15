package cc.cryptopunks.ui.model

import cc.cryptopunks.ui.model.internal.args
import cc.cryptopunks.ui.model.internal.calculateScore
import cc.cryptopunks.ui.model.internal.hasSingleTextInput
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow

typealias UIUpdates = List<UI.Update<*, *>>

enum class UIMode { View, Command }

enum class UIDisplay { Panel, Data, Input, Method }

data class UIMethod(
    val method: Service.Method,
    val elements: List<Element>,
) {
    val score by lazy { calculateScore() }
    val args by lazy { args() }
    val isReady by lazy { args.keys == method.params.keys }
    val singleTextArg by lazy { hasSingleTextInput() }

    data class Element(
        val type: Type,
        val value: Any,
    )

    sealed interface Type {
        object Unknown : Type
        object MethodName : Type
        data class ArgName(val name: String) : Type
        data class ArgType(val name: String) : Type
        data class ArgValue(val name: String, val complex: Boolean) : Type
    }
}

typealias UIArgs = Map<String, Any>

data class UIParam(
    val name: String,
    val type: Service.Type,
    val resolvers: Iterable<UIResolver>
)

sealed class UIResolver(private val ordinal: Int) : Comparable<UIResolver> {
    override fun compareTo(other: UIResolver): Int = ordinal.compareTo(other.ordinal)
    data class Data(val view: UIView) : UIResolver(1)
    data class Option(val list: List<String>) : UIResolver(2)
    data class Method(val method: Service.Method, val path: Path) : UIResolver(3)
    data class Path(val chunks: List<String>, val single: Boolean = true)
    data class Input(val type: String) : UIResolver(4)
}

class UIView(val source: Service.Method, val args: UIArgs, val data: () -> Flow<JsonNode>) {
    override fun toString(): String = source.javaClass.simpleName + args.toString()
}

data class UIConfig(
    val map: Map<String, Any> = mapOf(
        "autoFill" to true,
        "autoExecute" to true,
    )
) : Map<String, Any> by map {
    val autoFill: Boolean by map
    val autoExecute: Boolean by map
}

data class UIData(
    val type: Service.Type,
    val value: Any,
)

data class UILayout(
    val type: Service.Type = Service.Type.Empty,
    val header: Single = Single.Empty,
    val content: Many = Many.Empty,
    val footer: Single = Single.Empty,
) {
    companion object {
        val Empty = UILayout()
    }

    sealed interface Element {
        val path: List<String>
    }

    data class Many(
        val elements: Single = Single.Empty,
        override val path: List<String> = emptyList(),
    ) : Element {
        companion object {
            val Empty = Many()
        }
    }

    data class Single(
        val type: Service.Type = Service.Type.Empty,
        override val path: List<String> = emptyList(),
    ) : Element {
        companion object {
            val Empty = Single()
        }
    }
}
