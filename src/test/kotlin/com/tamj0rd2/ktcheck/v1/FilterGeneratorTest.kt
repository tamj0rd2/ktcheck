package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.GenerationException.FilterLimitReached
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTree
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.filter
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.ignoreExceptions
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.map
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.samples
import com.tamj0rd2.ktcheck.v1.GenV1Tests.Companion.expectGenerationAndShrinkingToEventuallyComplete
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo


internal class FilterGeneratorTest : BaseV1GeneratorTest() {
    @Nested
    inner class PredicateFiltering {
        @Test
        fun `can filter generated values`() {
            val gen = GenV1.int(1..10).filter { it % 2 == 0 }

            gen.samples()
                .take(100)
                .forEach { expectThat(it % 2).isEqualTo(0) }
        }

        @Test
        fun `throws if the filter threshold is exceeded`() {
            val gen = GenV1.int(1..10).filter { it > 10 }

            expectThrows<FilterLimitReached> { gen.samples().first() }
        }

        @Test
        fun `doesn't produce shrinks that would fail the predicate, which would otherwise lead to infinite shrinking`() {
            val gen = GenV1.int(1..4).filter { it > 2 }
            val tree = producerTree { left(4) }

            val (value, shrunkValues) = gen.generateWithShrunkValues(tree)
            expectThat(value).isEqualTo(4)
            expectThat(shrunkValues.toList())
                .describedAs("shrunk values")
                .isNotEmpty()
                .all { isGreaterThan(2) }
            gen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
        }
    }

    @Nested
    inner class IgnoreExceptions {
        @Test
        fun `can ignore exceptions in generated values`() {
            class TestException : Exception()

            val possiblyThrowingGen = GenV1.bool()
                .map { if (it) throw TestException() else false }
                .ignoreExceptions(TestException::class)

            val values = possiblyThrowingGen.samples().take(100).toList()
            expectThat(values).all { isFalse() }
        }

        @Test
        fun `doesn't produce shrinks that would cause the exception, which would otherwise lead to infinite shrinking`() {
            class TestException : Exception()

            val possiblyThrowingGen = GenV1.int(1..3)
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

            val (value, shrunkValues) = possiblyThrowingGen.generateWithShrunkValues(tree)
            expectThat(value).isEqualTo(3)
            expectThat(shrunkValues.toList())
                .describedAs("shrunk values")
                .isNotEmpty()
                .all { isNotEqualTo(1) }
            possiblyThrowingGen.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired = false)
        }

        @Test
        fun `if an ignored exception is thrown more times than the threshold, throws an error`() {
            class IgnoredException : Exception()

            val throwingGen = GenV1.bool()
                .map { throw IgnoredException() }
                .ignoreExceptions(IgnoredException::class)

            expectThrows<FilterLimitReached> { throwingGen.samples().first() }
        }

        @Test
        fun `if a non-ignored exception is thrown, it propagates`() {
            class IgnoredException : Exception()
            class NotIgnoredException : Exception()

            val throwingGen = GenV1.bool()
                .map { throw NotIgnoredException() }
                .ignoreExceptions(IgnoredException::class)

            expectThrows<NotIgnoredException> { throwingGen.samples().first() }
        }

        @Test
        fun `can ignore multiple exceptions types`() {
            class IgnoredException1 : Exception()
            class IgnoredException2 : Exception()

            val possiblyThrowingGen = GenV1.int(1..3)
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
}
