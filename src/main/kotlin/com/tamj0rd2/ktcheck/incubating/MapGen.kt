package com.tamj0rd2.ktcheck.incubating

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        val result = wrappedGen.generate(tree)
        return GenResultV2(
            value = fn(result.value),
            tree = tree,
            shrinks = result.shrinks,
        )
    }

    override fun edgeCases(tree: RandomTree): List<GenResultV2<R>> {
        return emptyList()
    }
}
