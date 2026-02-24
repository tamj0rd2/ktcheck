package com.tamj0rd2.ktcheck.incubating

internal class FlatMapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> GenImpl<R>,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GenResultV2<R> {
        val outerResult = wrappedGen.generate(root.left)
        val innerResult = fn(outerResult.value).generate(root.right)
        return buildResult(root = root, outerResult = outerResult, innerResult = innerResult)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<R>> {
        return wrappedGen.edgeCases(root.left).flatMap { outerEdgeCase ->
            fn(outerEdgeCase.value).edgeCases(root.right).map { innerEdgeCase ->
                val stableRoot = root
                    .withLeft(outerEdgeCase.tree)
                    .withRight(innerEdgeCase.tree)

                buildResult(root = stableRoot, outerResult = outerEdgeCase, innerResult = innerEdgeCase)
            }
        }
    }

    private fun buildResult(
        root: RandomTree,
        outerResult: GenResultV2<T>,
        innerResult: GenResultV2<R>,
    ): GenResultV2<R> {
        val outerBasedShrinks = outerResult.shrinks.map { root.withLeft(it) }
        val innerBasedShrinks = innerResult.shrinks.map { root.withRight(it) }
        val shrinks = outerBasedShrinks + innerBasedShrinks

        return GenResultV2(
            value = innerResult.value,
            tree = root,
            shrinks = shrinks,
        )
    }
}
