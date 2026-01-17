package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.GenerationException.ConditionalLogicDetectedDuringCombine
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.startsWith

internal interface CombinerGeneratorContract : BaseContract {
    @Test
    fun `generators using the builder shrink correctly`() {
        data class Person(val name: String, val age: Int)

        val gen = combine {
            Person(
                name = char('a'..'d').string(1..5).bind(),
                age = int(0..10).bind()
            )
        }

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
    fun `shrinking throws an error when conditionals affect a tail bind call`() {
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

        try {
            result.shrunkValues.toList()
            fail("Expected V2 to fail on conditional affecting bind")
        } catch (_: ConditionalLogicDetectedDuringCombine) {
            // expected
        }
    }

    @Test
    fun `shrinking throws an error when conditionals affect a middle bind call`() {
        data class XY(val x: Int?, val y: Int)

        val gen = combine {
            val includeX = bool().bind()

            if (includeX) {
                val x = int(1..3).bind()
                val y = int(4..6).bind()
                XY(x, y)
            } else {
                // Problem: y would consume shrinks meant for x
                val y = int(4..6).bind()
                XY(null, y)
            }
        }

        val result = gen.generating(XY(3, 6))

        try {
            result.deeplyShrunkValues.toList()
            fail("Expected generation/shrinking to fail due to incorrect tree position consumption, but it succeeded")
        } catch (_: ConditionalLogicDetectedDuringCombine) {
            // expected
        }
    }

    @Test
    fun `how to deal with conditionals inside combine correctly`() {
        data class XY(val x: Int?, val y: Int)

        val gen = combine {
            val includeX = bool().bind()
            val x = int(1..3).bind()
            val y = int(4..6).bind()

            if (includeX) XY(x, y) else XY(null, y)
        }

        val result = gen.generating(XY(3, 6))
        expectDoesNotThrow { result.deeplyShrunkValues.toList() }
    }
}
