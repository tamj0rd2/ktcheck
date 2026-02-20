package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import kotlin.random.Random

internal data class RandomTree(
    override val data: Seed,
    override val lazyLeft: Lazy<RandomTree>,
    override val lazyRight: Lazy<RandomTree>,
) : Tree<Seed>() {
    val random get() = Random(data.value)

    override val left: RandomTree get() = lazyLeft.value
    override val right: RandomTree get() = lazyRight.value

    fun withLeft(left: RandomTree): RandomTree = copy(lazyLeft = lazyOf(left))
    fun withRight(right: RandomTree): RandomTree = copy(lazyRight = lazyOf(right))
}

internal fun randomTree(seed: Seed = Seed.random()): RandomTree = RandomTree(
    data = seed,
    lazyLeft = lazy { randomTree(seed.next(1)) },
    lazyRight = lazy { randomTree(seed.next(2)) },
)
