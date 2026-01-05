package com.tamj0rd2.ktcheck.gen

private class ConstantGenerator<T>(private val value: T) : Gen<T>() {
    override fun GenContext.generate(): GenResult<T> = GenResult(value, emptySequence())
}

fun <T> Gen.Companion.constant(value: T): Gen<T> = ConstantGenerator(value)

fun Gen.Companion.char(
    chars: Iterable<Char> = Char.MIN_VALUE..Char.MAX_VALUE,
): Gen<Char> = Gen.oneOf(chars.distinct().sorted())
