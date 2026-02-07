package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker

internal class ListGen<T>(
    private val gen: GenImpl<T>,
    private val sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val sizeGen = IntGen(sizeRange, IntShrinker.defaultShrinkTarget(sizeRange))

    override fun edgeCases(): List<GenResultV2<List<T>>> {
        val cases = mutableListOf<GenResultV2<List<T>>>()

        // todo: I don't like any of this.
        // Empty list if size range allows
        if (0 in sizeRange) {
            cases.add(buildResult(emptySequence(), emptyList()))
        }

        // Singleton lists with element edge cases
        if (1 in sizeRange) {
            gen.edgeCases().forEach { elementEdgeCase ->
                cases.add(buildResult(emptySequence(), listOf(elementEdgeCase)))
            }
        }

        // Duplicate lists with element edge cases
        if (2 in sizeRange) {
            gen.edgeCases().forEach { elementEdgeCase ->
                cases.add(buildResult(emptySequence(), listOf(elementEdgeCase, elementEdgeCase)))
            }
        }

        return cases
    }

    override fun generate(tree: RandomTree): GenResultV2<List<T>> {
        val (size, sizeShrinks) = sizeGen.generate(tree.left)
        val elementResults = generateListWithResults(tree.right, size)
        return buildResult(sizeShrinks, elementResults)
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
                    val updatedElementResults = elementResults.mapIndexed { i, result ->
                        if (i == index) shrunkElementResult else result
                    }
                    yield(buildResult(sizeShrinks, updatedElementResults))
                }
            }
        }
    )

    private fun generateListWithResults(
        tree: RandomTree,
        size: Int,
    ): List<GenResultV2<T>> {
        val results = mutableListOf<GenResultV2<T>>()
        var currentTree = tree

        repeat(size) {
            val elementResult = gen.generate(currentTree.left)
            results.add(elementResult)
            currentTree = currentTree.right
        }

        return results
    }
}
