package com.tamj0rd2.ktcheck.incubating

internal sealed class AbstractListGen<T>(
    protected val elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : GenImpl<List<T>>() {
    protected val sizeGen = IntGen(sizeRange, sizeRange.first)

    final override fun generate(root: RandomTree): GenResultV2<List<T>> {
        val sizeResult = sizeGen.generate(root.left)
        val listElementResults = generateElements(root.right, sizeResult.value)
        return buildResult(root, sizeResult, listElementResults)
    }

    protected fun buildResult(
        root: RandomTree,
        sizeResult: GenResultV2<Int>,
        listElementResults: List<GenResultV2<T>>,
    ): GenResultV2<List<T>> {
        val sizeShrinks = sizeResult.shrinks.flatMap { sizeShrink ->
            val removeElementsFromTail = root.withSizeTree(sizeShrink)

            val newSize = sizeGen.generate(sizeShrink).value
            val removeElementsFromHead = root
                .withSizeTree(sizeShrink)
                .withElementTrees(listElementResults.takeLast(newSize))

            sequenceOf(removeElementsFromTail, removeElementsFromHead)
        }

        val elementBasedShrinks = listElementResults.asSequence().flatMapIndexed { index, elementResult ->
            elementResult.shrinks.map { shrink ->
                root.withElementTrees(listElementResults.map { it.tree }.replaceAtIndex(index, shrink))
            }
        }

        return GenResultV2(
            value = listElementResults.map { it.value },
            tree = root,
            shrinks = sizeShrinks + elementBasedShrinks,
        )
    }

    protected abstract fun generateElements(initialTree: RandomTree, size: Int): List<GenResultV2<T>>

    protected fun RandomTree.withSizeTree(sizeShrink: RandomTree) = withLeft(sizeShrink)

    @JvmName("withElementResults")
    protected fun RandomTree.withElementTrees(elementResults: List<GenResultV2<T>>) =
        withElementTrees(elementResults.map { it.tree })

    protected fun RandomTree.withElementTrees(elementTrees: List<RandomTree>): RandomTree {
        if (elementTrees.isEmpty()) return this

        // todo: make this tail recursive.
        fun RandomTree.replaceLeftTree(index: Int): RandomTree = when {
            index >= elementTrees.size -> {
                RandomTree.terminal
            }

            else -> {
                val newTree = elementTrees[index]
                withLeft(newTree).withRight(right.replaceLeftTree(index + 1))
            }
        }

        return withRight(replaceLeftTree(0))
    }

    protected fun <T> List<T>.replaceAtIndex(index: Int, replacement: T): List<T> =
        toMutableList().apply { set(index, replacement) }
}
