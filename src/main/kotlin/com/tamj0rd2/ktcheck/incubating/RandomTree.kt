package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import kotlin.random.Random
import kotlin.random.nextInt

@ConsistentCopyVisibility
internal data class RandomTree private constructor(
    val provider: ValueProvider,
    override val lazyLeft: Lazy<RandomTree>,
    override val lazyRight: Lazy<RandomTree>,
) : Tree<ValueProvider>() {
    override fun toString(): String = visualise(maxDepth = 10)

    override val data = provider

    override val left: RandomTree get() = lazyLeft.value
    override val right: RandomTree get() = lazyRight.value

    fun traversingRight() = generateSequence(this) { it.right }

    // todo: make this a tree type of its own, rather than a provider?
    val isTerminator: Boolean get() = provider is TerminalValueProvider

    fun withPredeterminedValue(value: Int): RandomTree =
        copy(provider = PredeterminedValueProvider(value, provider))

    fun withLeft(left: RandomTree): RandomTree = copy(lazyLeft = lazyOf(left))
    fun withRight(right: RandomTree): RandomTree = copy(lazyRight = lazyOf(right))

    fun skipRight(amount: Int) = walkRight(this, amount)

    fun replaceLeftAtOffset(rightOffset: Int, newLeftTree: RandomTree): RandomTree {
        return walkRightAndReplaceLeftTrees(listOf(rightOffset to newLeftTree), null)
    }

    fun walkRightAndReplaceLeftTrees(
        newTrees: List<Pair<Int, RandomTree>>,
        terminator: RandomTree?,
    ): RandomTree {
        if (newTrees.isEmpty()) return this

        val indicesToReplace = newTrees.map { it.first }.toSet()
        val maxIndex = indicesToReplace.max()
        val newTrees = newTrees.sortedBy { it.first }.map { it.second }
        var newTreeMarker = 0

        // todo: make this tail recursive.
        fun RandomTree.replaceLeftTree(index: Int): RandomTree = when {
            index > maxIndex -> {
                terminator ?: this
            }

            index !in indicesToReplace -> {
                withRight(right.replaceLeftTree(index + 1))
            }

            else -> {
                val newTree = newTrees[newTreeMarker]
                newTreeMarker += 1
                withLeft(newTree).withRight(right.replaceLeftTree(index + 1))
            }
        }

        return replaceLeftTree(0)
    }


    companion object {
        fun new(seed: Seed = Seed.random()): RandomTree = RandomTree(
            provider = RandomValueProvider(seed),
            lazyLeft = lazy { new(seed.next(1)) },
            lazyRight = lazy { new(seed.next(2)) },
        )

        val terminal
            get(): RandomTree = RandomTree(
                provider = TerminalValueProvider,
                lazyLeft = lazy { terminal },
                lazyRight = lazy { terminal },
            )

        val forEdgeCases = new(Seed(0))

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

private data class RandomValueProvider(private val seed: Seed) : ValueProvider {
    private val random get() = Random(seed.value)

    override fun int(range: IntRange): Int {
        return random.nextInt(range)
    }
}

@ConsistentCopyVisibility
private data class PredeterminedValueProvider private constructor(
    private val value: Any,
    override val delegate: ValueProvider,
) : DecoratedValueProvider {
    constructor(value: Int, fallback: ValueProvider) : this(value as Any, fallback)

    override fun int(range: IntRange): Int =
        when (value) {
            !is Int,
            !in range,
                -> delegate.int(range)

            else -> value
        }
}
