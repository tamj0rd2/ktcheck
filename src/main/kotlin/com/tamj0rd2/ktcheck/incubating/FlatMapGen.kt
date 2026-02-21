package com.tamj0rd2.ktcheck.incubating

internal class FlatMapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> GenImpl<R>,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GenResultV2<R> {
        val outerResult = wrappedGen.generate(root.left)
        val innerResult = fn(outerResult.value).generate(root.right)

        val outerBasedShrinks = outerResult.shrinks.map { root.withLeft(it) }
        val innerBasedShrinks = innerResult.shrinks.map { root.withRight(it) }
        val shrinks = outerBasedShrinks + innerBasedShrinks

        return GenResultV2(
            value = innerResult.value,
            tree = root,
            shrinks = shrinks,
        )
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<R>> {
        return emptyList()
    }
}
