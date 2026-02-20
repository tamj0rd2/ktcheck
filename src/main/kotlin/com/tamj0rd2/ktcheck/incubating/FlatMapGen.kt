package com.tamj0rd2.ktcheck.incubating

internal class FlatMapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> GenImpl<R>,
) : GenImpl<R>() {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        val outerResult = wrappedGen.generate(tree.left)
        val innerResult = fn(outerResult.value).generate(tree.right)

        val outerBasedShrinks = outerResult.shrinks.map { tree.withLeft(it) }
        val innerBasedShrinks = innerResult.shrinks.map { tree.withRight(it) }
        val shrinks = outerBasedShrinks + innerBasedShrinks

        return GenResultV2(
            value = innerResult.value,
            shrinks = shrinks,
        )
    }

    override fun edgeCases(): List<GenResultV2<R>> {
        return emptyList()
    }
}
