package com.tamj0rd2.ktcheck.incubating

internal class ListGen<T>(
    private val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val sizeGen = IntGen(sizeRange, sizeRange.first)

    override fun generate(tree: RandomTree): GenResultV2<List<T>> {
        val sizeResult = sizeGen.generate(tree.left)
        val listElementResults = generateElements(tree.right, sizeResult.value)
        return buildResult(tree, sizeResult, listElementResults)
    }

    override fun edgeCases(): List<GenResultV2<List<T>>> {
        return sizeGen.edgeCases().flatMap { sizeResult ->
            elementGen.edgeCases().map { elementResult ->
                val elementResults = List(sizeResult.value) { elementResult }
                buildResult(RandomTree.forEdgeCases, sizeResult, elementResults)
            }
        }
    }

    private fun buildResult(
        tree: RandomTree,
        sizeResult: GenResultV2<Int>,
        listElementResults: List<GenResultV2<T>>,
    ): GenResultV2<List<T>> {
        val sizeBasedShrinks = sizeResult.shrinks.flatMap {
            sequence {
                val tailRemovalShrink = tree.withLeft(it)
                yield(tailRemovalShrink)

                val newSize = sizeGen.generate(it).value
                val elementGenOffset = sizeResult.value - newSize
                val headRemovalShrink = tree.withLeft(it).withRight(tree.skipRight(elementGenOffset + 1))
                yield(headRemovalShrink)
            }
        }

        val elementBasedShrinks = listElementResults.asSequence().flatMapIndexed { index, elementResult ->
            elementResult.shrinks.map { shrink ->
                tree.replaceLeftAtOffset(rightOffset = index + 1, newLeftTree = shrink)
            }
        }

        return GenResultV2(
            value = listElementResults.map { it.value },
            shrinks = sizeBasedShrinks + elementBasedShrinks,
        )
    }

    private fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): MutableList<GenResultV2<T>> {
        val results = mutableListOf<GenResultV2<T>>()
        var tree = initialTree

        repeat(size) {
            val elementResult = elementGen.generate(tree.left)
            results.add(elementResult)
            tree = tree.right
        }

        return results
    }
}
