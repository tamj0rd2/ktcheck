package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.CombinerContext
import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.core.ProducerTree

internal class CombinerGenerator<T>(
    private val block: (CombinerContext) -> T,
) : GenV1<T>() {
    override fun GenContext.generate(): GenResult<T> {
        val initialTree = tree
        return CombinerContextV1(initialTree, mode).run {
            val value = block(this)
            val shrinks = initialTree.combineShrinks(shrinksByIndex)
            GenResult(value, shrinks)
        }
    }
}

class CombinerContextV1 internal constructor(
    private var tree: ProducerTree,
    private val mode: GenMode,
) : CombinerContext {
    internal val shrinksByIndex = mutableListOf<Sequence<ProducerTree>>()

    override fun <T> Gen<T>.bind(): T {
        val (value, shrinks) = (this as GenV1).generate(tree.left, mode)
        tree = tree.right
        shrinksByIndex.add(shrinks)
        return value
    }
}
