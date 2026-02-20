package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.GenerationException.FilterLimitReached
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import java.time.Duration

internal interface FilterGeneratorContract : BaseContract {
    @Test
    fun `can filter generated values and their shrinks`() {
        val gen = int(1..10).filter { it % 2 == 0 }
        repeatTest { seed ->
            val result = gen.generate(tree(seed))
            expectThat(result).value.assertThat("is even") { it % 2 == 0 }
            expectThat(result).shrunkValues.all { assertThat("is even") { it % 2 == 0 } }
        }

        // todo: add some - deeply shrunk values are finite function. call it above.
        gen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
    }

    @Test
    // todo: this should only happen if it matches the filter.
    fun `filter preserves edge cases from underlying generator`() {
        val gen = int(0..100).filter { it % 2 == 0 }

        val edgeCaseValues = gen.edgeCases().map { it.value }.toList()

        expectThat(edgeCaseValues).contains(listOf(0, 100))
    }

    @Test
    fun `throws if the filter threshold is exceeded`() {
        val gen = int(1..10).filter { it > 10 }
        expectThrows<FilterLimitReached> { gen.generate() }
    }

    @Test
    fun `can ignore exceptions in generated values and shrinks`() {
        class TestException : Exception()

        val possiblyThrowingGen = int(1..3)
            .map {
                when (it) {
                    1 -> throw TestException()
                    else -> it
                }
            }
            .ignoreExceptions(TestException::class)

        withCounter {
            repeatTest { seed ->
                val result = possiblyThrowingGen.generate(tree(seed))
                expectThat(result).value.isNotEqualTo(1)
                expectThat(result).shrunkValues.all { isNotEqualTo(1) }
                collect("has-shrinks", result.shrunkValues.isNotEmpty())
            }
        }.checkPercentages("has-shrinks", mapOf(true to 45.0))

        // todo: add some - deeply shrunk values are finite function. call it above.
        possiblyThrowingGen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
    }

    @Test
    fun `if an ignored exception is thrown more times than the threshold, throws an error`() {
        class IgnoredException : Exception()

        val throwingGen = bool()
            .map { throw IgnoredException() }
            .ignoreExceptions(IgnoredException::class)

        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            expectThrows<FilterLimitReached> { throwingGen.samples().first() }
        }
    }

    @Test
    fun `if a non-ignored exception is thrown, it propagates`() {
        class IgnoredException : Exception()
        class NotIgnoredException : Exception()

        val throwingGen = bool()
            .map { throw NotIgnoredException() }
            .ignoreExceptions(IgnoredException::class)

        expectThrows<NotIgnoredException> { throwingGen.generate() }
    }

    @Test
    fun `can ignore multiple exceptions types`() {
        class IgnoredException1 : Exception()
        class IgnoredException2 : Exception()

        val possiblyThrowingGen = int(1..3)
            .map {
                when (it) {
                    1 -> throw IgnoredException1()
                    2 -> throw IgnoredException2()
                    else -> it
                }
            }
            .ignoreExceptions(IgnoredException1::class)
            .ignoreExceptions(IgnoredException2::class)

        repeatTest { seed ->
            val result = possiblyThrowingGen.generate(tree(seed))
            expectThat(result).value.isEqualTo(3)
        }
    }

    @Test
    fun `ignoreExceptions propagates edge cases from underlying generator`() {
        Assumptions.assumeTrue(false)
        TODO("write this test")
    }
}
