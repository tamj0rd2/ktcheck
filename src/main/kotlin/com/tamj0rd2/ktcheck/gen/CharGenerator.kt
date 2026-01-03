package com.tamj0rd2.ktcheck.gen

import com.tamj0rd2.ktcheck.producer.ProducerTree

private class CharGenerator(chars: Iterable<Char>) : Gen<Char>() {
    private val chars = chars.distinct().sorted()

    override fun generate(tree: ProducerTree): GenResult<Char> {
        val char = tree.producer.char(chars.toSet())
        val shrinks = char.shrink().map { tree.withValue(it) }
        return GenResult(char, shrinks)
    }

    private fun Char.shrink(): Sequence<Char> {
        return shrink(chars.indexOf(this), 0..<chars.size).map { chars[it] }
    }
}

fun Gen.Companion.char(
    chars: Iterable<Char> = Char.MIN_VALUE..Char.MAX_VALUE,
): Gen<Char> = CharGenerator(chars)
