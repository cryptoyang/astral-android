package cc.cryptopunks.binary

import java.nio.ByteBuffer

val Byte.bytes: ByteArray get() = ByteArray(1) { this }
val Short.bytes: ByteArray get() = ByteBuffer.allocate(2).putShort(this).array()
val Int.bytes: ByteArray get() = ByteBuffer.allocate(4).putInt(this).array()
val Long.bytes: ByteArray get() = ByteBuffer.allocate(8).putLong(this).array()

val ByteArray.byte: Byte get() = this[0]
val ByteArray.short: Short get() = ByteBuffer.wrap(this).short
val ByteArray.int: Int get() = ByteBuffer.wrap(this).int
val ByteArray.long: Long get() = ByteBuffer.wrap(this).long
