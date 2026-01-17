package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.v1.V1BaseContract
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isIn
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.startsWith

internal interface CombinerGeneratorContract : BaseContract {
    @Test
    fun `generators using the builder shrink correctly`() {
        data class Person(val name: String, val age: Int)

        val nameGen = char('a'..'d').string(1..5)
        val ageGen = int(0..10)
        val gen = combine { Person(name = nameGen.bind(), age = ageGen.bind()) }

        val result = gen.generating {
            // limiting values to something that can definitely shrink
            it.age > 0 && !it.name.startsWith("a")
        }
        expectThat(result.value) {
            get { age } isGreaterThan (0)
            get { name }.not().startsWith('a')
        }

        // todo: can remove .take(1000) once V1 is gone.
        expectThat(result.deeplyShrunkValues.take(1000).toSet())
            .isNotEmpty()
            .any { get { name }.isEqualTo(result.value.name.take(1)) }
            .any { get { age }.isEqualTo(0) }
            .contains(Person(name = "a", age = 0))
    }

    @Test
    fun `conditionals work correctly when only affecting the last bind`() {
        data class XY(val x: Int, val y: Int?)

        val gen = combine {
            val includeY = bool().bind()
            val x = int(0..<10).bind()

            if (includeY) {
                val y = int(10..20).bind()
                XY(x, y)
            } else {
                XY(x, null)
            }
        }

        val result = gen.generating(XY(5, 15))

        // When includeY shrinks to false, we only bind 2 generators and get XY(5, null)
        expectThat(result.shrunkValues).isNotEmpty().contains(XY(5, null))

        // all shrinks meet their constraints
        expectThat(result.shrunkValues.map { it.x }).all { isIn(0..<10) }
        expectThat(result.shrunkValues.mapNotNull { it.y }).all { isIn(10..20) }
    }

    @Test
    fun `conditionals fail when affecting a middle bind`() {
        Assumptions.assumeTrue(this is V1BaseContract)
        data class XY(val x: Int?, val y: Int)

        // When a conditional affects a bind that is NOT the last one, subsequent binds
        // consume the wrong tree positions, leading to incorrect values.

        val gen = combine {
            val includeX = bool().bind()

            if (includeX) {
                val x = int(1..3).bind()
                val y = int(4..6).bind()
                XY(x, y)
            } else {
                // Problem: y will consume position 2 instead of position 3!
                val y = int(4..6).bind()
                XY(null, y)
            }
        }

        // When includeX shrinks to false, y incorrectly consumes position 2 (value 5) instead of position 3 (value 15).
        // This demonstrates the combiner doesn't handle non-tail conditional binds well.
        val result = gen.generating(XY(3, 6))
        try {
            result.deeplyShrunkValues.toSet().forEach { println(it) }
            fail("Expected generation/shrinking to fail due to incorrect tree position consumption")
        } catch (e: IllegalStateException) {
            expectThat(e.message).isNotNull().contains("not in range 4..6")
        }
    }
}
