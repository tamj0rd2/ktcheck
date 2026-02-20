package com.tamj0rd2.ktcheck.incubating

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        val (value, shrinks) = wrappedGen.generate(tree)
        return GenResultV2(
            value = fn(value),
            shrinks = shrinks,
        )
    }

    override fun edgeCases(): List<GenResultV2<R>> {
        return emptyList()
    }
}
