package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import kotlin.random.Random
import kotlin.random.nextInt

internal data class RandomTree(
    val provider: ValueProvider,
    override val lazyLeft: Lazy<RandomTree>,
    override val lazyRight: Lazy<RandomTree>,
) : Tree<ValueProvider>() {
    override val data = provider

    override val left: RandomTree get() = lazyLeft.value
    override val right: RandomTree get() = lazyRight.value

    fun withProvider(provider: ValueProvider): RandomTree = copy(provider = provider)
    fun withLeft(left: RandomTree): RandomTree = copy(lazyLeft = lazyOf(left))
    fun withRight(right: RandomTree): RandomTree = copy(lazyRight = lazyOf(right))

    companion object {
        fun new(seed: Seed = Seed.random()): RandomTree = RandomTree(
            provider = RandomValueProvider(seed),
            lazyLeft = lazy { new(seed.next(1)) },
            lazyRight = lazy { new(seed.next(2)) },
        )

        val forEdgeCases = new(Seed(0))
    }
}

internal sealed interface ValueProvider {
    fun int(range: IntRange): Int
}

internal data class RandomValueProvider(private val seed: Seed) : ValueProvider {
    private val random get() = Random(seed.value)

    override fun int(range: IntRange): Int {
        return random.nextInt(range)
    }
}

@ConsistentCopyVisibility
internal data class PredeterminedValueProvider private constructor(private val value: Any) : ValueProvider {
    constructor(value: Int) : this(value as Any)

    override fun int(range: IntRange): Int {
        check(value is Int) { "expected an Int but got ${value::class.simpleName}" }
        check(value in range) { "expected an Int in range $range but got $value" }
        return value
    }
}
