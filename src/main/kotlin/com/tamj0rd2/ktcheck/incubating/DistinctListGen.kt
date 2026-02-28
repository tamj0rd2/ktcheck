package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class DistinctListGen<T>(
    private val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    private val sizeGen = IntGen(sizeRange, sizeRange.first)

    private fun debug(value: Any = "") {
        //println(value)
    }

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
        val sizeShrinks = sizeResult.shrinks.flatMap { sizeShrink ->
            val appliedSizeShrink = root.withLeft(sizeShrink)

            val newSize = sizeGen.generate(sizeShrink).value
            // todo: convenience functions around this would be nice. doing the same thing in ListGen, and multiple
            //  times across this file
            val headRemovalShrink = appliedSizeShrink.withRight(
                root.right.walkRightAndReplaceLeftTrees(
                    newTrees = listElementResults.takeLast(newSize).map { it.tree },
                    terminator = RandomTree.terminal,
                )
            )
            sequenceOf(appliedSizeShrink, headRemovalShrink)
        }

        val elementBasedShrinks = listElementResults.asSequence().flatMapIndexed { index, elementResult ->
            elementResult.shrinks.map { shrink ->
                root.withRight(
                    root.right.walkRightAndReplaceLeftTrees(
                        newTrees = listElementResults.mapIndexed { idx, it -> if (idx == index) shrink else it.tree },
                        terminator = RandomTree.terminal,
                    )
                )
            }
        }

        return GenResultV2(
            value = listElementResults.map { it.value }.also {
                debug("Produced $it from:")
                debug(root)
                debug("=======")
                debug()
            },
            tree = root,
            shrinks = sizeShrinks + elementBasedShrinks,
        )
    }

    private fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): MutableList<GenResultV2<T>> {
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
                debug("Generated ${elementResult.value} from ${tree.left.provider}")
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

    private companion object {
        const val MAX_ATTEMPTS_PER_ELEMENT = 100
    }
}

private fun <T> List<T>.replaceAtIndex(index: Int, value: T): List<T> = toMutableList().apply { set(index, value) }
