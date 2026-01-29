package com.tamj0rd2.ktcheck.core

import kotlin.random.Random

@ConsistentCopyVisibility
internal data class ProducerTree private constructor(
    private val seed: Seed,
    private val lazyLeft: Lazy<ProducerTree>,
    private val lazyRight: Lazy<ProducerTree>,
) {
    val random get() = Random(seed.value)
    val left: ProducerTree by lazyLeft
    val right: ProducerTree by lazyRight

    companion object {
        internal fun new(seed: Seed = Seed.random()): ProducerTree = ProducerTree(
            seed = seed,
            lazyLeft = lazy { new(seed.next(1)) },
            lazyRight = lazy { new(seed.next(2)) },
        )
    }

    internal fun withLeft(left: ProducerTree) = copy(lazyLeft = lazyOf(left))
    internal fun withRight(right: ProducerTree) = copy(lazyRight = lazyOf(right))

    override fun toString(): String = visualise(maxDepth = 10)

    @Suppress("unused")
    internal fun visualise(maxDepth: Int = 3, forceEval: Boolean = false): String {
        fun visualise(
            tree: ProducerTree,
            indent: String,
            prefix: String,
            isLast: Boolean?,
            currentDepth: Int,
        ): String {
            if (currentDepth >= maxDepth) return "${indent}${prefix}...\n"

            fun visualiseBranch(lazyTree: Lazy<ProducerTree>, side: String): String? {
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
                appendLine("${indent}${prefix}${tree.seed}")
                visualiseBranch(tree.lazyLeft, "L")?.let(::append)
                visualiseBranch(tree.lazyRight, "R")?.let(::append)
            }
        }

        return visualise(tree = this, indent = "", prefix = "", isLast = null, currentDepth = 0)
    }
}
