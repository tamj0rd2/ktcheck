package com.tamj0rd2.ktcheck.gen

import java.util.*

private class ConstantGenerator<T>(private val value: T) : Gen<T>() {
    override fun GenContext.generate(): GenResult<T> = GenResult(value, emptySequence())
}

fun <T> Gen.Companion.constant(value: T): Gen<T> = ConstantGenerator(value)

fun Gen.Companion.char(
    chars: Iterable<Char> = Char.MIN_VALUE..Char.MAX_VALUE,
): Gen<Char> = Gen.oneOf(chars.distinct().sorted())

fun Gen<Char>.string(size: IntRange): Gen<String> = list(size).map { it.joinToString("") }
fun Gen<Char>.string(size: Int) = string(size..size)

fun Gen.Companion.uuid(): Gen<UUID> = (Gen.long() + Gen.long()).map { UUID(it.first, it.second) }
