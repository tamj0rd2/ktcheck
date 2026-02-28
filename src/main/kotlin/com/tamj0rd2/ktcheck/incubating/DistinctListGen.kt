package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class DistinctListGen<T>(
    private val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val minSize = sizeRange.first
    private val sizeGen = IntGen(sizeRange, minSize)

    override fun generate(root: RandomTree): GenResultV2<List<T>> {
        val sizeResult = sizeGen.generate(root.left)
        val listElementResults = generateElements(root.right, sizeResult.value)
        return buildResult(root, sizeResult, listElementResults)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<List<T>>> {
        return emptyList()
    }

    private fun buildResult(
        root: RandomTree,
        sizeResult: GenResultV2<Int>,
        listElementResults: List<GenResultV2<T>>,
    ): GenResultV2<List<T>> {
        return GenResultV2(
            value = listElementResults.map { it.value },
            tree = root,
            shrinks = emptySequence(),
        )
    }

    private fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): MutableList<GenResultV2<T>> {
        val results = mutableListOf<GenResultV2<T>>()
        val seenValues = mutableSetOf<T>()
        var tree = initialTree
        var attempts = 0

        while (results.size < size) {
            attempts += 1

            val elementResult = elementGen.generate(tree.left)

            if (seenValues.add(elementResult.value)) {
                results.add(elementResult)
                attempts = 0
                continue
            }

            if (attempts >= MAX_ATTEMPTS_PER_ELEMENT) {
                throw GenerationException.DistinctCollectionSizeImpossible(
                    minSize = minSize,
                    achievedSize = results.size,
                    attempts = attempts,
                )
            }

            tree = tree.right
        }

        return results
    }

    private companion object {
        const val MAX_ATTEMPTS_PER_ELEMENT = 100
    }
}
