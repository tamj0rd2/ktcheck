package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTree
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

internal interface CombinatorGeneratorContract : BaseContract {
    @Test
    fun `constant always produces the same value and doesn't shrink`() {
        // todo: move this test elsewhere.
        val (value, shrinks) = constant(10).generateWithShrunkValues(ProducerTree.new())
        expectThat(value).isEqualTo(10)
        expectThat(shrinks).isEmpty()
    }

    @Test
    fun `map maps the original value and shrinks`() {
        val originalGen = int(0..10)
        val doublingGen = originalGen.map { it * 2 }

        val tree = ProducerTree.new()
        val (originalNumber, originalShrinks) = originalGen.generateWithShrunkValues(tree)
        val (doubledValue, doubledShrinks) = doublingGen.generateWithShrunkValues(tree)

        expectThat(doubledValue).isEqualTo(originalNumber * 2)
        expectThat(doubledShrinks).isEqualTo(originalShrinks.map { it * 2 })
    }

    @Test
    fun `flatMap generates the second value based on the first`() {
        val smallGen = int(0..5)
        val bigGen = int(10..20)
        val gen = smallGen.flatMap { a -> bigGen.map { b -> a + b } }

        val tree = producerTree {
            left(5)
            right(20)
        }

        val value = gen.generate(tree)
        expectThat(value).isEqualTo(25)
    }

    @Test
    fun `flatMap combines shrinks from both generators`() {
        val gen = int(1..3).flatMap { outer ->
            int(4..6).map { inner ->
                Pair(outer, inner)
            }
        }

        val tree = producerTree {
            left(3)
            right(6)
        }

        val (value, shrinks) = gen.generateWithDeepShrinks(tree)
        expectThat(value).isEqualTo(3 to 6)
        expectThat(shrinks.toList().distinct()).contains(
            3 to 4,
        )
    }

    @Test
    fun `combineWith merges two independent generators`() {
        val smallGen = int(0..5)
        val bigGen = int(10..20)
        val gen = smallGen.combineWith(bigGen) { a, b -> a + b }

        val tree = producerTree {
            left(5)
            right(20)
        }

        val value = gen.generate(tree)
        expectThat(value).isEqualTo(25)
    }

    @Test
    fun `combineWith combines shrinks from both generators`() {
        val smallGen = int(1..3)
        val bigGen = int(4..6)
        val gen = smallGen.combineWith(bigGen) { a, b -> a + b }

        val tree = producerTree {
            left(3)
            right(6)
        }

        val (value, shrinks) = gen.generateWithShrunkValues(tree)
        expectThat(value).isEqualTo(9)

        val threeShrunk = IntShrinker.shrink(3, 1..3)
        val sixShrunk = IntShrinker.shrink(6, 4..6)

        expectThat(shrinks).contains(threeShrunk.map { it + 6 }.toList())
        expectThat(shrinks).contains(sixShrunk.map { it + 3 }.toList())
    }

    @Test
    fun `same seed produces same sample`() {
        val seed = 12345L
        val gen = int(-1000..1000)

        val firstRun = gen.samples(seed).take(100).toList()
        val secondRun = gen.samples(seed).take(100).toList()

        expectThat(secondRun).isEqualTo(firstRun)
    }
}
