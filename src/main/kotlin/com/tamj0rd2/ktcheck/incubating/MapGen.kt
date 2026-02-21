package com.tamj0rd2.ktcheck.incubating

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GenResultV2<R> {
        val result = wrappedGen.generate(root)
        return GenResultV2(
            value = fn(result.value),
            tree = root,
            shrinks = result.shrinks,
        )
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<R>> {
        return emptyList()
    }
}
