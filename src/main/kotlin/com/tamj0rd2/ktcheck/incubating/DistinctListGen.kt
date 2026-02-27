package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class DistinctListGen<T>(
    private val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val sizeGen = IntGen(sizeRange, sizeRange.first)
    private val minSize = sizeRange.first

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

        // todo: this should be based on the tree offset, not size of the list
        val maximumTreeOffset = listElementResults.size - 1
        val elementBasedShrinks = listElementResults.asSequence().flatMapIndexed { index, elementResult ->
            elementResult.shrinks.map { shrink ->
                /**
                 * Seed: 2971460367603760984
                 * Generated 0, 4
                 * root.right.left generates 0
                 * root.right.left.right.left generates 4
                 * There are no shrinks for element 0. So tries to shrink the next element, i.e 4.
                 * That would usually shrink to 0, 0. However, that's not distinct.
                 * So the generation logic further below causes the next element to be used instead, which is actually an 8 because
                 * root.right.left.right.left.right.left generates 8
                 * That's why I end up with 0, 8 which is an invalid shrink (an element got bigger rather than smaller)
                 * So I probably need a way to stop the generation from proceeding past a particular index.
                 *
                 * The second issue here is that I'm using list indexes, rather than tree offsets. Fix the todos in this file.
                 */
                val newRightTree = root.right
                    .withMaximumTreeOffset(maximumTreeOffset)
                    // todo: index used here should be based on tree offset, not the list index.
                    .replaceLeftAtOffset(index, shrink)

                root.withRight(newRightTree)
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
        targetSize: Int,
    ): List<GenResultV2<T>> {
        val maximumTreeOffset = (initialTree.provider as? WithMaximumTreeOffset)?.maximumTreeOffset ?: Int.MAX_VALUE
        val results = mutableListOf<GenResultV2<T>>()
        val seenValues = mutableSetOf<T>()
        var tree = initialTree
        var treeOffset = 0
        var attempts = 0

        while (results.size < targetSize) {
            attempts += 1

            if (treeOffset > maximumTreeOffset) {
                throw GenerationException.DistinctCollectionSizeImpossible(
                    minSize = minSize,
                    achievedSize = seenValues.size,
                    attempts = attempts,
                )
            }

            try {
                val elementResult = elementGen.generate(tree.left)
                if (seenValues.add(elementResult.value)) {
                    results.add(elementResult)
                    attempts = 0
                    continue
                }

                if (attempts > MAX_ATTEMPTS_PER_ELEMENT) {
                    throw GenerationException.DistinctCollectionSizeImpossible(
                        minSize = minSize,
                        achievedSize = seenValues.size,
                        attempts = attempts,
                    )
                }
            } finally {
                tree = tree.right
                treeOffset += 1
            }
        }

        return results
    }

    private data class WithMaximumTreeOffset(
        val maximumTreeOffset: Int,
        override val delegate: ValueProvider,
    ) : DecoratedValueProvider, ValueProvider by delegate

    private fun RandomTree.withMaximumTreeOffset(maximumTreeOffset: Int) = withProvider(
        WithMaximumTreeOffset(maximumTreeOffset, provider)
    )

    private companion object {
        const val MAX_ATTEMPTS_PER_ELEMENT = 100
    }
}
