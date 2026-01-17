package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree

internal class ListGenV2<T>(
    private val gen: GenV2<T>,
    private val sizeGen: IntGenV2,
) : GenV2<List<T>> {

    override fun generate(tree: ProducerTree): GenResultV2<List<T>> {
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
        tree: ProducerTree,
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
