package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map

internal class MapGen<T, R>(
    private val wrappedGen: Generator<T>,
    private val fn: (T) -> R,
) : Generator<R> {
    override fun generate(root: RandomTree): Result4k<GeneratedValue<R>, GenerationException> {
        return wrappedGen.generate(root).map { it.map() }
    }

    override fun edgeCase(root: RandomTree, mode: GenerationMode): Result4k<GeneratedValue<R>?, GenerationException> {
        return wrappedGen.edgeCase(root, mode).map { it?.map() }
    }

    private fun GeneratedValue<T>.map(): GeneratedValue<R> = GeneratedValue(
        value = fn(value),
        shrinks = shrinks,
    )
}
