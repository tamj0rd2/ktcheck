package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree

internal class CombineGenV2<T1, T2, R>(
    private val gen1: GenV2<T1>,
    private val gen2: GenV2<T2>,
    private val combine: (T1, T2) -> R,
) : GenV2<R> {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        return combine(gen1.generate(tree.left), gen2.generate(tree.right))
    }

    override fun edgeCases(): List<GenResultV2<R>> {
        val leftEdgeCases = gen1.edgeCases()
        val rightEdgeCases = gen2.edgeCases()

        // Return cartesian product of edge cases from both generators
        return leftEdgeCases.flatMap { left ->
            rightEdgeCases.map { right ->
                combine(left, right)
            }
        }
    }

    private fun combine(
        leftResult: GenResultV2<T1>,
        rightResult: GenResultV2<T2>,
    ): GenResultV2<R> {
        val (leftValue, leftShrinks) = leftResult
        val (rightValue, rightShrinks) = rightResult

        val leftBasedShrinks = leftShrinks.map { combine(it, rightResult) }
        val rightBasedShrinks = rightShrinks.map { combine(leftResult, it) }

        // This helps find counterexamples where both values need to stay coupled
        val diagonalShrinks = leftShrinks.zip(rightShrinks).map { (leftShrink, rightShrink) ->
            combine(leftShrink, rightShrink)
        }

        return GenResultV2(
            value = combine(leftValue, rightValue),
            shrinks = diagonalShrinks + leftBasedShrinks + rightBasedShrinks,
        )
    }
}
