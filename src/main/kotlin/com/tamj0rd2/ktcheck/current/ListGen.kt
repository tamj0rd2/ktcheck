package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure

internal class ListGen<T>(
    elementGen: GenImpl<T>,
    sizeRange: IntRange,
) : AbstractListGen<T>(elementGen, sizeRange) {

    override fun generateElements(
        initialTree: RandomTree,
        size: Int,
    ): Result4k<List<WithUsedTree<GeneratedValue<T>>>, GenerationException> = buildList {
        for (tree in initialTree.traversingRight().take(size)) {
            add(
                elementGen.generate(tree.left)
                    .map { WithUsedTree(tree.left, it) }
                    .onFailure { return it }
            )
        }
    }.asSuccess()
}
