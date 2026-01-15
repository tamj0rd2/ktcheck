package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.core.ProducerTree

internal class CombinerGenerator<T>(
    private val block: CombinerContext.() -> T,
) : GenV1<T>() {
    override fun GenContext.generate(): GenResult<T> {
        val initialTree = tree
        return CombinerContext(initialTree, mode).run {
            val value = block(this)
            val shrinks = initialTree.combineShrinks(shrinksByIndex)
            GenResult(value, shrinks)
        }
    }
}

class CombinerContext internal constructor(
    private var tree: ProducerTree,
    private val mode: GenMode,
) {
    internal val shrinksByIndex = mutableListOf<Sequence<ProducerTree>>()

    fun <T> GenV1<T>.bind(): T {
        val (value, shrinks) = generate(tree.left, mode)
        tree = tree.right
        shrinksByIndex.add(shrinks)
        return value
    }
}
