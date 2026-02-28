package com.tamj0rd2.ktcheck.incubating

internal class ListGen<T>(
    elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : AbstractListGen<T>(elementGen, sizeRange) {

    override fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): List<GenResultV2<T>> = initialTree.traversingRight()
        .map { elementGen.generate(it.left) }
        .take(size)
        .toList()

    override fun edgeCases(root: RandomTree): List<GenResultV2<List<T>>> {
        return sizeGen.edgeCases(root.left).flatMap { sizeResult ->
            elementGen.edgeCases(root.right).map { elementResult ->
                val elementResults = List(sizeResult.value) { elementResult }

                val reproducibleTree = root
                    .withSizeTree(sizeResult.tree)
                    .withElementTrees(elementResults)

                buildResult(reproducibleTree, sizeResult, elementResults)
            }
        }
    }
}
