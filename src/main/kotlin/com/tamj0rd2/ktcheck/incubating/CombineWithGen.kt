package com.tamj0rd2.ktcheck.incubating

internal class CombineWithGen<T1, T2, R>(
    private val leftGen: GenImpl<T1>,
    private val rightGen: GenImpl<T2>,
    private val combine: (T1, T2) -> R,
) : GenImpl<R>() {
    override fun generate(root: RandomTree): GenResultV2<R> {
        val leftResult = leftGen.generate(root.left)
        val rightResult = rightGen.generate(root.right)
        return buildResult(root, leftResult, rightResult)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<R>> {
        val leftEdgeCases = leftGen.edgeCases(root.left)
        val rightEdgeCases = rightGen.edgeCases(root.right)

        if (leftEdgeCases.isEmpty() && rightEdgeCases.isEmpty()) {
            return emptyList()
        }

        val leftEdgeCasesToUse = leftEdgeCases.ifEmpty { listOf(leftGen.generate(root.left)) }
        val rightEdgeCasesToUse = rightEdgeCases.ifEmpty { listOf(rightGen.generate(root.right)) }

        return leftEdgeCasesToUse.flatMap { leftEdgeCase ->
            rightEdgeCasesToUse.map { rightEdgeCase ->
                val stableRoot = root
                    .withLeft(leftEdgeCase.tree)
                    .withRight(rightEdgeCase.tree)

                buildResult(stableRoot, leftEdgeCase, rightEdgeCase)
            }
        }
    }

    private fun buildResult(
        root: RandomTree,
        leftResult: GenResultV2<T1>,
        rightResult: GenResultV2<T2>,
    ): GenResultV2<R> {
        val leftBasedShrinks = leftResult.shrinks.map { root.withLeft(it) }
        val rightBasedShrinks = rightResult.shrinks.map { root.withRight(it) }
        val shrinks = leftBasedShrinks + rightBasedShrinks

        return GenResultV2(
            value = combine(leftResult.value, rightResult.value),
            tree = root,
            shrinks = shrinks,
        )
    }
}
