package cc.cryptopunks.ui.poc.model

data class UILayout(
    val type: Api.Type = Api.Type.Empty,
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
        val type: Api.Type = Api.Type.Empty,
        override val path: List<String> = emptyList(),
    ) : Element {
        companion object {
            val Empty = Single()
        }
    }
}
