package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree

internal class MapGenV2<T, R>(
    private val gen: GenV2<T>,
    private val fn: (T) -> R,
) : GenV2<R> {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        return gen.generate(tree).map(fn)
    }
}
