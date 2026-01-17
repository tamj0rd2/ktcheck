package com.tamj0rd2.ktcheck.core

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.trees
import com.tamj0rd2.ktcheck.v1.GenMode
import com.tamj0rd2.ktcheck.v1.GenV1
import com.tamj0rd2.ktcheck.v2.GenV2

internal class ProducerTreeDsl(private var subject: ProducerTree) {
    fun left(tree: ProducerTree) {
        subject = subject.withLeft(tree)
    }

    fun right(tree: ProducerTree) {
        subject = subject.withRight(tree)
    }

    fun right(block: ProducerTreeDsl.() -> Unit = {}) {
        val newRightTree = ProducerTreeDsl(subject.right).apply(block).subject
        subject = subject.withRight(newRightTree)
    }

    companion object {
        fun tree(seed: Seed = Seed.random(), block: ProducerTreeDsl.() -> Unit): ProducerTree =
            ProducerTreeDsl(ProducerTree.new(seed)).apply(block).subject

        fun trees(seed: Seed = Seed.random()) =
            Seed.sequence(seed).map { ProducerTree.new(it) }
    }
}

internal fun treeWhere(predicate: (ProducerTree) -> Boolean): ProducerTree =
    trees().take(1_000_000).first(predicate)

internal fun <T> Gen<T>.findTreeProducing(value: T): ProducerTree =
    findTreeProducing { it == value }

internal fun <T> Gen<T>.findTreeProducing(predicate: (T) -> Boolean): ProducerTree =
    treeWhere { tree: ProducerTree ->
        val value = when (this) {
            is GenV1 -> generate(tree, GenMode.Initial).value
            is GenV2 -> generate(tree).value
            else -> error("Unsupported Gen type: ${this::class}")
        }

        predicate(value)
    }
