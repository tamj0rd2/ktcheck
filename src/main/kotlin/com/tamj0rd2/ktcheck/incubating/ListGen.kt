package com.tamj0rd2.ktcheck.incubating

internal class ListGen<T>(
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
        return sizeGen.edgeCases(root.left).flatMap { sizeResult ->
            elementGen.edgeCases(root.right).map { elementResult ->
                val elementResults = List(sizeResult.value) { elementResult }

                val elementResultsTree = root.right.walkRightAndReplaceLeftTrees(
                    elementResults.mapIndexed { index, elementResult -> index to elementResult.tree }
                )

                val reproducibleTree = root
                    .withLeft(sizeResult.tree)
                    .withRight(elementResultsTree)

                buildResult(reproducibleTree, sizeResult, elementResults)
            }
        }
    }

    private fun buildResult(
        root: RandomTree,
        sizeResult: GenResultV2<Int>,
        listElementResults: List<GenResultV2<T>>,
    ): GenResultV2<List<T>> {
        val sizeBasedShrinks = sizeResult.shrinks.flatMap {
            sequence {
                val tailRemovalShrink = root.withLeft(it)
                yield(tailRemovalShrink)

                val newSize = sizeGen.generate(it).value
                val elementGenOffset = sizeResult.value - newSize
                val headRemovalShrink = root.withLeft(it).withRight(root.skipRight(elementGenOffset + 1))
                yield(headRemovalShrink)
            }
        }

        val elementBasedShrinks = listElementResults.asSequence().flatMapIndexed { index, elementResult ->
            elementResult.shrinks.map { shrink ->
                root.replaceLeftAtOffset(rightOffset = index + 1, newLeftTree = shrink)
            }
        }

        return GenResultV2(
            value = listElementResults.map { it.value },
            tree = root,
            shrinks = sizeBasedShrinks + elementBasedShrinks,
        )
    }

    private fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): List<GenResultV2<T>> {
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
