package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import com.tamj0rd2.ktcheck.current.GenerationMode.*
import kotlin.random.nextInt

internal data class TreeData(
    val provider: ValueProvider,
    val mode: GenerationMode,
)

@ConsistentCopyVisibility
internal data class RandomTree private constructor(
    override val data: TreeData,
    override val lazyLeft: Lazy<RandomTree>,
    override val lazyRight: Lazy<RandomTree>,
) : Tree<TreeData>() {
    override fun toString(): String = visualise(maxDepth = 10)

    val provider = data.provider
    val generationMode = data.mode

    override val left: RandomTree get() = lazyLeft.value
    override val right: RandomTree get() = lazyRight.value

    fun traversingRight() = generateSequence(this) { it.right }

    // todo: make this a tree type of its own, rather than a provider?
    val isTerminator: Boolean get() = provider is TerminalValueProvider

    fun switchToRandomGeneration() = when (data.mode) {
        Random -> this
        Shrinking -> this
        EdgeCase -> copy(data = data.copy(mode = Random))
    }

    fun switchToEdgeCaseMode() = when (data.mode) {
        Random -> copy(data = data.copy(mode = EdgeCase))
        Shrinking -> this
        EdgeCase -> this
    }

    fun withShrunkValue(value: Any): RandomTree =
        copy(
            data = data.copy(
                provider = PredeterminedValueProvider(value, provider),
                mode = Shrinking,
            )
        )

    fun withLeft(left: RandomTree): RandomTree = copy(lazyLeft = lazyOf(left))
    fun withRight(right: RandomTree): RandomTree = copy(lazyRight = lazyOf(right))

    fun skipRight(amount: Int) = walkRight(this, amount)

    companion object {
        fun new(seed: Seed = Seed.random()): RandomTree {
            val provider = RandomValueProvider(seed)
            return RandomTree(
                data = TreeData(
                    provider = provider,
                    // todo: inject edge case generation chance.
                    mode = if (provider.random.nextDouble() > 0.08) Random else EdgeCase
                ),
                lazyLeft = lazy { new(seed.next(1)) },
                lazyRight = lazy { new(seed.next(2)) },
            )
        }

        val terminal
            get(): RandomTree = RandomTree(
                data = TreeData(
                    provider = TerminalValueProvider,
                    mode = Random
                ),
                lazyLeft = lazy { terminal },
                lazyRight = lazy { terminal },
            )

        private tailrec fun walkRight(tree: RandomTree, amount: Int): RandomTree = when (amount) {
            0 -> tree
            else -> walkRight(tree.right, amount - 1)
        }
    }
}

internal sealed interface ValueProvider {
    fun int(range: IntRange): Int
}

internal interface DecoratedValueProvider : ValueProvider {
    val delegate: ValueProvider
}

data object TerminalValueProvider : ValueProvider {
    override fun int(range: IntRange): Int {
        error("${TerminalValueProvider::class.simpleName} cannot produce values")
    }
}

internal data class RandomValueProvider(private val seed: Seed) : ValueProvider {
    val random get() = kotlin.random.Random(seed.value)

    override fun int(range: IntRange): Int {
        return random.nextInt(range)
    }
}

internal data class PredeterminedValueProvider(
    private val value: Any,
    override val delegate: ValueProvider,
) : DecoratedValueProvider {
    init {
        val valueIsSupported = when (value) {
            is Int -> true
            else -> false
        }
        require(valueIsSupported) { "internal error - predetermined value was not a supported primitive" }
    }

    override fun int(range: IntRange): Int =
        when (value) {
            !is Int,
            !in range,
                -> delegate.int(range)

            else -> value
        }
}
