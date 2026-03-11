package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k

internal class EdgeCasesDisabledGen<T>(
    private val delegate: Generator<T>,
) : Generator<T> by delegate {
    override fun generate(root: RandomTree): Result4k<GeneratedValue<T>, GenerationException> {
        return delegate.generate(root.withoutEdgeCases())
    }
}
