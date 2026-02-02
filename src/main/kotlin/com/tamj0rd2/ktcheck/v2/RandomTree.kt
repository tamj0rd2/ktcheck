package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import kotlin.random.Random

internal typealias RandomTree = Tree<Seed>

internal val RandomTree.random get() = Random(data.value)

internal fun randomTree(seed: Seed = Seed.random()): RandomTree = Tree(
    data = seed,
    lazyLeft = lazy { randomTree(seed.next(1)) },
    lazyRight = lazy { randomTree(seed.next(2)) },
)
