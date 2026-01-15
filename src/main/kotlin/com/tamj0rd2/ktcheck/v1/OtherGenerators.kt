package com.tamj0rd2.ktcheck.v1

import java.util.*

private class ConstantGenerator<T>(private val value: T) : GenV1<T>() {
    override fun GenContext.generate(): GenResult<T> = GenResult(value, emptySequence())
}

fun <T> GenV1.Companion.constant(value: T): GenV1<T> = ConstantGenerator(value)

fun GenV1.Companion.char(
    chars: Iterable<Char> = Char.MIN_VALUE..Char.MAX_VALUE,
): GenV1<Char> = GenV1.oneOf(chars.distinct().sorted())

fun GenV1<Char>.string(size: IntRange): GenV1<String> = list(size).map { it.joinToString("") }
fun GenV1<Char>.string(size: Int) = string(size..size)

fun GenV1.Companion.uuid(): GenV1<UUID> = (GenV1.long() + GenV1.long()).map { UUID(it.first, it.second) }
