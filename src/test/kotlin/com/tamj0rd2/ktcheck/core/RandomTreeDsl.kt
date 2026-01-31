package com.tamj0rd2.ktcheck.core

internal class RandomTreeDsl(private var subject: RandomTree) {
    fun left(tree: RandomTree) {
        subject = subject.withLeft(tree)
    }

    fun right(tree: RandomTree) {
        subject = subject.withRight(tree)
    }

    companion object {
        fun tree(seed: Seed = Seed.random(), block: RandomTreeDsl.() -> Unit): RandomTree =
            RandomTreeDsl(RandomTree.new(seed)).apply(block).subject

        fun treeWhere(seed: Seed = Seed.random(), predicate: (RandomTree) -> Boolean): RandomTree =
            trees(seed).take(1_000_000).first(predicate)

        fun trees(seed: Seed = Seed.random()) =
            Seed.sequence(seed).map { RandomTree.new(it) }
    }
}
