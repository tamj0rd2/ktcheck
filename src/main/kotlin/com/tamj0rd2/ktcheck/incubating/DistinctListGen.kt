package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class DistinctListGen<T>(
    elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : AbstractListGen<T>(elementGen, sizeRange) {

    override fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): List<GenResultV2<T>> {
        val trees = initialTree.traversingRight().iterator()
        val results = mutableListOf<GenResultV2<T>>()
        val seenValues = mutableSetOf<T>()
        var attempts = 0

        while (results.size < size) {
            val tree = trees.next()

            if (tree.isTerminator) {
                throw GenerationException.DistinctCollectionSizeImpossible(
                    minSize = size,
                    achievedSize = results.size,
                    attempts = attempts,
                )
            }

            attempts += 1

            val elementResult = elementGen.generate(tree.left)

            if (seenValues.add(elementResult.value)) {
                results.add(elementResult)
                attempts = 0
                continue
            }

            if (attempts >= MAX_ATTEMPTS_PER_ELEMENT) {
                throw GenerationException.DistinctCollectionSizeImpossible(
                    minSize = size,
                    achievedSize = results.size,
                    attempts = attempts,
                )
            }
        }

        return results
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<List<T>>> {
        return emptyList()
    }

    private companion object {
        const val MAX_ATTEMPTS_PER_ELEMENT = 100
    }
}
