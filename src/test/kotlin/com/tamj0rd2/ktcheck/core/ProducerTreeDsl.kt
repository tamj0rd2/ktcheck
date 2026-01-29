package com.tamj0rd2.ktcheck.core

internal class ProducerTreeDsl(private var subject: ProducerTree) {
    fun left(tree: ProducerTree) {
        subject = subject.withLeft(tree)
    }

    fun right(tree: ProducerTree) {
        subject = subject.withRight(tree)
    }

    companion object {
        fun tree(seed: Seed = Seed.random(), block: ProducerTreeDsl.() -> Unit): ProducerTree =
            ProducerTreeDsl(ProducerTree.new(seed)).apply(block).subject

        fun treeWhere(seed: Seed = Seed.random(), predicate: (ProducerTree) -> Boolean): ProducerTree =
            trees(seed).take(1_000_000).first(predicate)

        fun trees(seed: Seed = Seed.random()) =
            Seed.sequence(seed).map { ProducerTree.new(it) }
    }
}
