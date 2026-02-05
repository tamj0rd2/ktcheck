package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException.DistinctCollectionSizeImpossible

internal class DistinctListGen<T>(
    private val gen: GenImpl<T>,
    private val sizeRange: IntRange,
) : GenImpl<List<T>>() {
    override fun generate(tree: RandomTree): GenResultV2<List<T>> {
        val sizeResult = IntGen(sizeRange).generate(tree.left)
        val elementResults = generateListWithResults(
            tree = tree.right,
            minSize = sizeRange.first,
            maxSize = sizeResult.value,
        )

        val actualSize = elementResults.size

        val sizeResultToUse = sizeResult.takeIf { it.value == actualSize }
            ?: IntGen(sizeRange).generate(tree.left)

        return buildResult(sizeResultToUse.shrinks, elementResults)
    }

    private fun buildResult(
        sizeShrinks: Sequence<GenResultV2<Int>>,
        elementResults: List<GenResultV2<T>>,
    ): GenResultV2<List<T>> = GenResultV2(
        value = elementResults.map { it.value },
        shrinks = sequence {
            sizeShrinks.forEach { sizeResult ->
                val (shrunkSize, sizeShrinks) = sizeResult
                when (shrunkSize) {
                    0 -> yield(buildResult(emptySequence(), emptyList()))
                    else -> {
                        yield(buildResult(sizeShrinks, elementResults.take(shrunkSize)))
                        yield(buildResult(sizeShrinks, elementResults.takeLast(shrunkSize)))
                    }
                }
            }

            elementResults.forEachIndexed { index, elementResult ->
                elementResult.shrinks.forEach { shrunkElementResult ->
                    val updatedElementResults = elementResults
                        .mapIndexed { i, result -> if (i == index) shrunkElementResult else result }
                        .distinctBy { it.value }

                    if (updatedElementResults.size in sizeRange) {
                        yield(buildResult(sizeShrinks, updatedElementResults))
                    }
                }
            }
        }
    )

    private fun generateListWithResults(
        tree: RandomTree,
        minSize: Int,
        maxSize: Int,
    ): List<GenResultV2<T>> {
        val results = mutableListOf<GenResultV2<T>>()
        val seenValues = mutableSetOf<T>()
        var failureCount = 0

        val trees = generateSequence(tree) { it.right }
            .takeWhile { failureCount < MAX_FAILURES }
            .iterator()

        while (results.size < maxSize) {
            if (!trees.hasNext()) {
                if (results.size >= minSize) break

                throw DistinctCollectionSizeImpossible(
                    minSize = minSize,
                    achievedSize = results.size,
                    attempts = failureCount,
                )
            }

            val result = gen.generate(trees.next().left)

            if (seenValues.add(result.value)) {
                results.add(result)
                failureCount = 0
            } else {
                failureCount += 1
            }
        }

        return results
    }

    companion object {
        private const val MAX_FAILURES = 100
    }
}
