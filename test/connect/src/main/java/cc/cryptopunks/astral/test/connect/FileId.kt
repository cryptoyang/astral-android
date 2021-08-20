package cc.cryptopunks.astral.test.connect

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class FileId(
    val size: ULong,
    val hash: ByteArray,
) {
    init {
        require(hash.size == 32)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileId

        if (size != other.size) return false
        if (!hash.contentEquals(other.hash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + hash.contentHashCode()
        return result
    }
}

private const val idPrefix = "id1"

fun FileId.pack(): ByteArray =
    ByteArray(8).also {
        ByteBuffer.wrap(it).order(ByteOrder.BIG_ENDIAN).putLong(size.toLong())
    } + hash

fun ByteArray.unpackFileId(): FileId {
    require(size == 32)
    return FileId(
        size = ByteBuffer.wrap(copyOf(8)).order(ByteOrder.BIG_ENDIAN).long.toULong(),
        hash = copyOfRange(8, 40)
    )
}
