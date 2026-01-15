package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTree
import com.tamj0rd2.ktcheck.core.Seed
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal interface OneOfGeneratorContract : BaseContract {
    @Test
    fun `can choose between generators uniformly`() {
        val gen = oneOf(
            bool().map { it as Any },
            int().map { it as Any },
        )

        withCounter { gen.samples().take(100_000).forEach { collect(it::class.simpleName) } }
            .checkPercentages(mapOf("Boolean" to 49.0, "Int" to 49.0))
    }

    @Test
    fun `shrinking a oneOf generator can shrink between types without failure`() {
        val multiTypeGen = oneOf(
            bool().map { it as Any },
            int(0..4).map { it as Any },
        )

        val tree = producerTree(Seed(1)) {
            left(1)
            right(4)
        }

        val (originalValue, shrinks) = multiTypeGen.generateWithShrunkValues(tree)

        expectThat(originalValue).isEqualTo(4)
        expectThat(shrinks.toList()).isEqualTo(
            listOf(
                // Choice shrunk from 1 to 0. So generating a Boolean value:
                true,
                // Left shrinks complete. So Choice = 1. Now shrinking Int value (4):
                0,
                2,
                3,
            )
        )
    }

    @Test
    fun `oneOfValues shrinks toward first value in collection`() {
        val values = listOf("banana", "apple", "cherry")
        val gen = oneOf(values)

        withCounter {
            gen.samples().take(100_000).forEach { collect(it) }
        }.checkPercentages(values.associateWith { 32.0 })

        val tree = ProducerTree.new().withValue(2)
        val (value, shrinks) = gen.generateWithShrunkValues(tree)
        expectThat(value).isEqualTo("cherry")

        expectThat(shrinks.toList()).isEqualTo(listOf("banana", "apple"))
    }
}
