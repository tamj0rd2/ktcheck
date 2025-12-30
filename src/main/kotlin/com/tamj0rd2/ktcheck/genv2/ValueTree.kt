package com.tamj0rd2.ktcheck.genv2

import com.tamj0rd2.ktcheck.gen.deriveSeed
import kotlin.random.Random
import kotlin.random.nextInt

internal sealed interface Value {
    fun int(range: IntRange): Int
    fun bool(): Boolean

    data class Undetermined(val seed: Long) : Value {
        private val random get() = Random(seed)

        override fun int(range: IntRange): Int = random.nextInt(range)

        override fun bool(): Boolean = random.nextBoolean()
    }

    data class Predetermined(val value: Any?) : Value {
        init {
            if (value is Value) error("Nested Value instances are not allowed. Developer error?")
        }

        override fun int(range: IntRange): Int = when (value) {
            !is Int -> TODO("handle this")
            !in range -> TODO("handle int out of range")
            else -> value
        }

        override fun bool(): Boolean {
            if (value !is Boolean) TODO("handle this")
            return value
        }
    }
}

@ConsistentCopyVisibility
internal data class ValueTree private constructor(
    internal val value: Value,
    private val lazyLeft: Lazy<ValueTree>,
    private val lazyRight: Lazy<ValueTree>,
) {
    companion object {
        internal fun fromSeed(seed: Long): ValueTree = ValueTree(
            value = Value.Undetermined(seed),
            lazyLeft = lazy { fromSeed(deriveSeed(seed, 1)) },
            lazyRight = lazy { fromSeed(deriveSeed(seed, 2)) },
        )
    }

    val left: ValueTree by lazyLeft
    val right: ValueTree by lazyRight

    internal fun withValue(value: Any?) = copy(value = Value.Predetermined(value))

    internal fun withLeft(left: ValueTree) = copy(lazyLeft = lazyOf(left))

    internal fun withRight(right: ValueTree) = copy(lazyRight = lazyOf(right))

    @Suppress("unused")
    internal fun visualise(maxDepth: Int = 3, forceEval: Boolean = false): String {
        fun visualise(
            tree: ValueTree,
            indent: String,
            prefix: String,
            isLast: Boolean?,
            currentDepth: Int,
        ): String {
            if (currentDepth >= maxDepth) return "${indent}${prefix}...\n"

            val displayValue = when (val value = tree.value) {
                is Value.Undetermined -> "seed(${value.seed})"
                is Value.Predetermined -> "value(${value.value})"
            }

            fun visualiseBranch(lazyTree: Lazy<ValueTree>, side: String): String? {
                val newIndent = when (isLast) {
                    null -> "" // Root level, no indentation
                    true -> "$indent    "
                    false -> "$indent│   "
                }

                if (!lazyTree.isInitialized() && !forceEval) return null

                return visualise(
                    tree = lazyTree.value,
                    indent = newIndent,
                    prefix = "├─$side: ",
                    isLast = false,
                    currentDepth = currentDepth + 1
                )
            }

            return buildString {
                appendLine("${indent}${prefix}${displayValue}")
                visualiseBranch(tree.lazyLeft, "L")?.let(::append)
                visualiseBranch(tree.lazyRight, "R")?.let(::append)
            }
        }

        return visualise(tree = this, indent = "", prefix = "", isLast = null, currentDepth = 0)
    }
}
