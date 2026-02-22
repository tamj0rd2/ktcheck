package com.tamj0rd2.ktcheck.incubating

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GenResultV2<R> {
        return wrappedGen.generate(root).map(fn)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<R>> {
        return wrappedGen.edgeCases(root).map { it.map(fn) }
    }

    fun GenResultV2<T>.map(fn: (T) -> R): GenResultV2<R> = GenResultV2(
        value = fn(value),
        tree = tree,
        shrinks = shrinks,
    )
}
