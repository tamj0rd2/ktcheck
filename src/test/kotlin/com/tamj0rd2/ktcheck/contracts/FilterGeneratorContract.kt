package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.GenerationException.FilterLimitReached
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo

internal interface FilterGeneratorContract : BaseContract {
    @Test
    fun `can filter generated values`() {
        val gen = int(1..10).filter { it % 2 == 0 }

        gen.samples()
            .take(100)
            .forEach { expectThat(it % 2).isEqualTo(0) }
    }

    @Test
    fun `throws if the filter threshold is exceeded`() {
        val gen = int(1..10).filter { it > 10 }

        expectThrows<FilterLimitReached> { gen.samples().first() }
    }

    @Test
    fun `doesn't produce shrinks that would fail the predicate, which would otherwise lead to infinite shrinking`() {
        val gen = int(1..4).filter { it > 2 }
        val tree = producerTree { left(4) }

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(4)
        expectThat(result.shrunkValues.toList())
            .describedAs("shrunk values")
            .isNotEmpty()
            .all { isGreaterThan(2) }
        gen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
    }

    @Test
    fun `can ignore exceptions in generated values`() {
        class TestException : Exception()

        val possiblyThrowingGen = bool()
            .map { if (it) throw TestException() else false }
            .ignoreExceptions(TestException::class)

        val values = possiblyThrowingGen.samples().take(100).toList()
        expectThat(values).all { isFalse() }
    }

    @Test
    fun `doesn't produce shrinks that would cause the exception, which would otherwise lead to infinite shrinking`() {
        class TestException : Exception()

        val possiblyThrowingGen = int(1..3)
            .map {
                when (it) {
                    1 -> throw TestException()
                    else -> it
                }
            }
            .ignoreExceptions(TestException::class)

        val tree = producerTree {
            left(3)
            right {
                left(2)
            }
        }

        val result = possiblyThrowingGen.generate(tree)
        expectThat(result.value).isEqualTo(3)
        expectThat(result.shrunkValues.toList())
            .describedAs("shrunk values")
            .isNotEmpty()
            .all { isNotEqualTo(1) }
        possiblyThrowingGen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
    }

    @Test
    fun `if an ignored exception is thrown more times than the threshold, throws an error`() {
        class IgnoredException : Exception()

        val throwingGen = bool()
            .map { throw IgnoredException() }
            .ignoreExceptions(IgnoredException::class)

        expectThrows<FilterLimitReached> { throwingGen.samples().first() }
    }

    @Test
    fun `if a non-ignored exception is thrown, it propagates`() {
        class IgnoredException : Exception()
        class NotIgnoredException : Exception()

        val throwingGen = bool()
            .map { throw NotIgnoredException() }
            .ignoreExceptions(IgnoredException::class)

        expectThrows<NotIgnoredException> { throwingGen.samples().first() }
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

        val values = possiblyThrowingGen.samples().take(100).toList()
        expectThat(values).all { isEqualTo(3) }
    }
}
