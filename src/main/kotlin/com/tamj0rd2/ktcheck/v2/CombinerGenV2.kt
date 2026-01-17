package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.CombinerContext
import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.core.ProducerTree

internal class CombinerGenV2<T>(
    private val block: (CombinerContext) -> T,
) : GenV2<T> {
    override fun generate(tree: ProducerTree): GenResultV2<T> {
        val context = CombinerContextV2(tree)
        val value = block(context)
        return buildResult(value, context.resultsByIndex)
    }

    private fun buildResult(value: T, resultsByIndex: List<GenResultV2<*>>): GenResultV2<T> {
        // For each result, yield shrinks where we use the shrunk value at that position
        val shrinks = sequence {
            resultsByIndex.forEachIndexed { index, result ->
                yieldAll(result.shrinks.map { shrunkResult ->
                    // Rebuild value with shrunk result at this index
                    val updatedResults = resultsByIndex.mapIndexed { i, r ->
                        if (i == index) shrunkResult else r
                    }
                    val replayContext = CombinerContextV2Replay(updatedResults)
                    val newValue = block(replayContext)
                    // Enable recursive shrinking by calling buildResult again
                    buildResult(newValue, updatedResults)
                })
            }
        }

        return GenResultV2(value, shrinks)
    }

    private class CombinerContextV2(
        private var tree: ProducerTree,
    ) : CombinerContext {
        val resultsByIndex = mutableListOf<GenResultV2<*>>()

        override fun <T> Gen<T>.bind(): T {
            val result = (this as GenV2).generate(tree.left)
            tree = tree.right
            resultsByIndex.add(result)
            return result.value
        }
    }

    private class CombinerContextV2Replay(
        private val results: List<GenResultV2<*>>,
    ) : CombinerContext {
        private var index = 0

        @Suppress("UNCHECKED_CAST")
        override fun <T> Gen<T>.bind(): T {
            val result = results[index] as GenResultV2<T>
            index++
            return result.value
        }
    }
}
