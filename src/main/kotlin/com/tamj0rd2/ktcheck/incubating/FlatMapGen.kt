package com.tamj0rd2.ktcheck.incubating

internal class FlatMapGen<T, R>(
    private val wrappedGen: GenImpl<T>,
    private val fn: (T) -> GenImpl<R>,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GeneratedValue<R> {
        val outerResult = wrappedGen.generate(root.left)
        val innerResult = fn(outerResult.value).generate(root.right)
        return buildResult(root = root, outerResult = outerResult, innerResult = innerResult)
    }

    override fun edgeCases(root: RandomTree): List<GeneratedValue<R>> {
        return wrappedGen.edgeCases(root.left).flatMap { outerEdgeCase ->
            fn(outerEdgeCase.value).edgeCases(root.right).map { innerEdgeCase ->
                val stableRoot = root
                    .withLeft(outerEdgeCase.usedTree)
                    .withRight(innerEdgeCase.usedTree)

                buildResult(root = stableRoot, outerResult = outerEdgeCase, innerResult = innerEdgeCase)
            }
        }
    }

    private fun buildResult(
        root: RandomTree,
        outerResult: GeneratedValue<T>,
        innerResult: GeneratedValue<R>,
    ): GeneratedValue<R> {
        val outerBasedShrinks = outerResult.shrinks.map { root.withLeft(it) }
        val innerBasedShrinks = innerResult.shrinks.map { root.withRight(it) }
        val shrinks = outerBasedShrinks + innerBasedShrinks

        return GeneratedValue(
            value = innerResult.value,
            shrinks = shrinks,
            usedTree = root,
        )
    }
}
