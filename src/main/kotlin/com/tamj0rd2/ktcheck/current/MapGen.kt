package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map

internal class MapGen<T, R>(
    private val wrappedGen: Generator<T>,
    private val fn: (T) -> R,
) : Generator<R> {
    override fun generate(root: RandomTree): Result4k<GeneratedValue<R>, GenerationException> =
        wrappedGen.generate(root).map {
            GeneratedValue(
                value = fn(it.value),
                shrinks = it.shrinks,
            )
        }
}
