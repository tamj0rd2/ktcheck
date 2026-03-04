package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R> {
    override fun generate(root: RandomTree): Result4k<GeneratedValue<R>, GenerationException> {
        return wrappedGen.generate(root).map { it.map(fn) }
    }

    override fun edgeCases(root: RandomTree): List<GeneratedValue<R>> {
        return wrappedGen.edgeCases(root).map { it.map(fn) }
    }

    fun GeneratedValue<T>.map(fn: (T) -> R): GeneratedValue<R> = GeneratedValue(
        value = fn(value),
        shrinks = shrinks,
        usedTree = usedTree,
    )
}
