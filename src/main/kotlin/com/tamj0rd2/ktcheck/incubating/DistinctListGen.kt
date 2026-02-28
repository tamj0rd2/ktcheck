package com.tamj0rd2.ktcheck.incubating

internal class DistinctListGen<T>(
    private val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val sizeGen = IntGen(sizeRange, sizeRange.first)

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

        while (results.size < size) {
            val elementResult = elementGen.generate(tree.left)

            if (seenValues.add(elementResult.value)) {
                results.add(elementResult)
            }

            tree = tree.right
        }

        return results
    }
}
