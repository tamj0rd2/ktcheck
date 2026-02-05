package com.tamj0rd2.ktcheck.current

internal class MapGen<T, R>(
    private val gen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        return gen.generate(tree).map(fn)
    }
}
