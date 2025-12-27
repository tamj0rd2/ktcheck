package com.tamj0rd2.ktcheck.genv2

import com.tamj0rd2.ktcheck.gen.deriveSeed
import kotlin.random.Random

internal sealed interface ChoiceTree {
    val left: ChoiceTree
    val right: ChoiceTree

    fun int(range: IntRange): Int

    companion object {
        internal fun from(seed: Long): ChoiceTree = RandomTree(
            seed = seed,
            lazyLeft = lazy { from(deriveSeed(seed, 1)) },
            lazyRight = lazy { from(deriveSeed(seed, 2)) },
        )
    }
}

internal data class RandomTree(
    private val seed: Long,
    private val lazyLeft: Lazy<ChoiceTree>,
    private val lazyRight: Lazy<ChoiceTree>,
) : ChoiceTree {
    private val random get() = Random(seed)

    override val left: ChoiceTree by lazyLeft
    override val right: ChoiceTree by lazyRight

    override fun int(range: IntRange) = range.random(random)
}

internal data class RecordedChoiceTree<out T>(
    private val original: ChoiceTree,
    private val choice: T,
) : ChoiceTree {
    override val left: ChoiceTree get() = original.left
    override val right: ChoiceTree get() = original.right

    override fun int(range: IntRange): Int {
        if (choice is Int) {
            return choice
        }

        TODO("Not yet implemented")
    }
}

internal fun combineShrinks(
    tree: ChoiceTree,
    leftShrinks: Sequence<ChoiceTree>,
    rightShrinks: Sequence<ChoiceTree>,
): Sequence<ChoiceTree> = TODO("Implement combineShrinks function for RandomTree")


private fun ChoiceTree.withLeft(left: ChoiceTree): ChoiceTree = when (this) {
    is RandomTree -> TODO()
    is RecordedChoiceTree<*> -> TODO()
}

private fun ChoiceTree.withRight(right: ChoiceTree): ChoiceTree = when (this) {
    is RandomTree -> TODO()
    is RecordedChoiceTree<*> -> TODO()
}
