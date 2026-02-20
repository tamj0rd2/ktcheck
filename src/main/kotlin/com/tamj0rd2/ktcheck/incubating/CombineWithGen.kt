package com.tamj0rd2.ktcheck.incubating

internal class CombineWithGen<T1, T2, R>(
    private val leftGen: GenImpl<T1>,
    private val rightGen: GenImpl<T2>,
    private val combine: (T1, T2) -> R,
) : GenImpl<R>() {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        val leftResult = leftGen.generate(tree.left)
        val rightResult = rightGen.generate(tree.right)
        return compose(tree, leftResult, rightResult)
    }

    private fun compose(
        tree: RandomTree,
        leftResult: GenResultV2<T1>,
        rightResult: GenResultV2<T2>,
    ): GenResultV2<R> {
        val leftBasedShrinks = leftResult.shrinks.map { tree.withLeft(it) }
        val rightBasedShrinks = rightResult.shrinks.map { tree.withRight(it) }
        val shrinks = leftBasedShrinks + rightBasedShrinks

        return GenResultV2(
            value = combine(leftResult.value, rightResult.value),
            shrinks = shrinks,
        )
    }

    override fun edgeCases(): List<GenResultV2<R>> {
        val leftEdgeCases = leftGen.edgeCases()
        val rightEdgeCases = rightGen.edgeCases()

        return leftEdgeCases.flatMap { leftEdgeCase ->
            rightEdgeCases.map { rightEdgeCases ->
                compose(RandomTree.forEdgeCases, leftEdgeCase, rightEdgeCases)
            }
        }
    }
}
