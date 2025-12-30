package com.tamj0rd2.ktcheck.genv2

import com.tamj0rd2.ktcheck.gen.deriveSeed
import kotlin.random.Random

internal sealed interface Choice {
    data class Undetermined(val seed: Long) : Choice
    data class Predetermined(val value: Any?) : Choice
}

internal sealed class ChoiceTree {
    protected abstract val choice: Choice
    abstract val left: ChoiceTree
    abstract val right: ChoiceTree

    abstract fun int(range: IntRange): Int

    companion object {
        internal fun from(seed: Long): ChoiceTree = RandomTree(
            choice = Choice.Undetermined(seed),
            lazyLeft = lazy { from(deriveSeed(seed, 1)) },
            lazyRight = lazy { from(deriveSeed(seed, 2)) },
        )

        @Suppress("unused")
        internal fun ChoiceTree.visualise(maxDepth: Int = 3, forceEval: Boolean = false): String = visualise(
            indent = "",
            prefix = "",
            isLast = null,
            currentDepth = 0,
            maxDepth = maxDepth,
            forceEval = forceEval,
        )

        private fun ChoiceTree.visualise(
            indent: String,
            prefix: String,
            isLast: Boolean?,
            currentDepth: Int,
            maxDepth: Int,
            forceEval: Boolean,
        ): String {
            if (currentDepth >= maxDepth) return "${indent}${prefix}...\n"

            val displayValue = when (val choice = choice) {
                is Choice.Undetermined -> "seed(${choice.seed})"
                is Choice.Predetermined -> "choice(${choice.value})"
            }

            val lazyLeft = when (this) {
                is RandomTree -> lazyLeft
                is RecordedChoiceTree -> lazyLeft
            }

            val lazyRight = when (this) {
                is RandomTree -> lazyRight
                is RecordedChoiceTree -> lazyRight
            }

            fun visualise(lazyTree: Lazy<ChoiceTree>, side: String): String? {
                val newIndent = when (isLast) {
                    null -> "" // Root level, no indentation
                    true -> "$indent    "
                    false -> "$indent│   "
                }

                if (!lazyTree.isInitialized() && !forceEval) return null

                return lazyTree.value.visualise(newIndent, "├─$side: ", false, currentDepth + 1, maxDepth, forceEval)
            }

            return buildString {
                appendLine("${indent}${prefix}${displayValue}")
                visualise(lazyLeft, "L")?.let(::append)
                visualise(lazyRight, "R")?.let(::append)
            }
        }
    }
}

internal data class RandomTree(
    override val choice: Choice.Undetermined,
    internal val lazyLeft: Lazy<ChoiceTree>,
    internal val lazyRight: Lazy<ChoiceTree>,
) : ChoiceTree() {
    private val random get() = Random(choice.seed)

    override val left: ChoiceTree by lazyLeft
    override val right: ChoiceTree by lazyRight

    override fun int(range: IntRange) = range.random(random)
}

internal data class RecordedChoiceTree(
    override val choice: Choice.Predetermined,
    internal val lazyLeft: Lazy<ChoiceTree>,
    internal val lazyRight: Lazy<ChoiceTree>,
) : ChoiceTree() {
    override val left: ChoiceTree by lazyLeft
    override val right: ChoiceTree by lazyRight

    override fun int(range: IntRange): Int {
        val value = choice.value

        if (value !is Int) TODO("handle this")
        if (value !in range) TODO("handle int out of range")
        return value
    }
}

internal fun <T> ChoiceTree.withChoice(value: T): ChoiceTree = when (this) {
    is RandomTree -> RecordedChoiceTree(
        choice = Choice.Predetermined(value),
        lazyLeft = lazyLeft,
        lazyRight = lazyRight,
    )

    is RecordedChoiceTree -> RecordedChoiceTree(
        choice = Choice.Predetermined(value),
        lazyLeft = lazyLeft,
        lazyRight = lazyRight,
    )
}

internal fun ChoiceTree.withLeft(left: ChoiceTree): ChoiceTree = when (this) {
    is RandomTree -> copy(lazyLeft = lazyOf(left))
    is RecordedChoiceTree -> copy(lazyLeft = lazyOf(left))
}

internal fun ChoiceTree.withRight(right: ChoiceTree): ChoiceTree = when (this) {
    is RandomTree -> copy(lazyRight = lazyOf(right))
    is RecordedChoiceTree -> copy(lazyRight = lazyOf(right))
}
