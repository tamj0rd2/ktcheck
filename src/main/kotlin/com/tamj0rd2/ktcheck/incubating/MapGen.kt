package com.tamj0rd2.ktcheck.incubating

internal class MapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> R,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GeneratedValue<R> {
        return wrappedGen.generate(root).map(fn)
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
