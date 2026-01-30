package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter
import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.NoOpTestReporter
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.TestByBool
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.checkAll
import com.tamj0rd2.ktcheck.forAll
import com.tamj0rd2.ktcheck.positive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import strikt.api.expectThrows
import java.time.Duration
import kotlin.math.abs

// based on https://github.com/jlink/shrinking-challenge/tree/main/challenges
internal interface ShrinkingChallengeContract : BaseContract {
    @Test
    fun `difference must not be zero`() {
        testShrinking(
            gen = int(IntRange.positive) + int(IntRange.positive),
            test = { it.first < 10 || abs(it.first - it.second) != 0 },
            didShrinkCorrectly = { it == Pair(10, 10) },
        )
    }

    @Test
    fun `difference must not be small`() {
        testShrinking(
            gen = int(IntRange.positive) + int(IntRange.positive),
            test = { it.first < 10 || abs(it.first - it.second) !in 1..4 },
            didShrinkCorrectly = { it == Pair(10, 6) },
        )
    }

    @Test
    fun `difference must not be one`() {
        testShrinking(
            gen = int(IntRange.positive) + int(IntRange.positive),
            test = { it.first < 10 || abs(it.first - it.second) != 1 },
            didShrinkCorrectly = { it == Pair(10, 9) },
        )
    }

    @Test
    fun distinct() {
        testShrinking(
            gen = int().list(),
            test = TestByBool { it.distinct().size < 3 },
            didShrinkCorrectly = { it.toSet() in setOf(setOf(0, 1, 2), setOf(0, -1, -2), setOf(0, 1, -1)) },
        )
    }

    @Test
    fun `large union list`() {
        testShrinking(
            gen = int().list(0..4).list(0..4),
            test = { it.flatten().toSet().size <= 4 },
            didShrinkCorrectly = { list ->
                val flattened = list.flatten()
                flattened.size == 5 && flattened.all { it in -4..4 }
            },
        )
    }

    @Test
    fun `length list`() {
        testShrinking(
            gen = int(0..1000).list(1..100),
            test = { it.max() < 900 },
            didShrinkCorrectly = { it == listOf(900) },
        )
    }

    @Test
    fun `nested lists`() {
        testShrinking(
            gen = int(Int.MIN_VALUE..Int.MAX_VALUE).list().list(),
            test = { listOfLists -> listOfLists.sumOf { it.size } <= 10 },
            // todo: although it works, it'd may be nice if later I can make it normalise the list to a single list.
            didShrinkCorrectly = { listOfLists ->
                val flattened = listOfLists.flatten()
                flattened.size == 11 && flattened.all { it == 0 }
            },
        )
    }

    @Test
    fun reverse() = testShrinking(
        gen = int().list(),
        test = { it.reversed() == it },
        didShrinkCorrectly = { it in setOf(listOf(0, 1), listOf(0, -1)) },
    )

    private fun <T> testShrinking(
        testConfig: TestConfig = TestConfig().withIterations(500),
        gen: Gen<T>,
        test: TestByBool<T>,
        didShrinkCorrectly: (T) -> Boolean,
        minConfidence: Double = 100.0,
        categoriseShrinks: Counter.(Boolean, T, T) -> Unit = { _, _, _ -> },
    ): Unit = assertTimeoutPreemptively(Duration.ofSeconds(5)) {
        val exceptionsWithBadShrinks = mutableListOf<PropertyFalsifiedException>()

        val counter = withCounter {
            checkAll(testConfig, long()) { seed ->
                val exception = expectThrows<PropertyFalsifiedException> {
                    forAll(TestConfig().withSeed(seed).withReporter(NoOpTestReporter), gen, test)
                }.subject

                @Suppress("UNCHECKED_CAST")
                val originalArgs = exception.originalResult.input as T

                @Suppress("UNCHECKED_CAST")
                val shrunkArgs = exception.smallestResult.input as T

                val fullyShrunk = didShrinkCorrectly(shrunkArgs)
                collect("fully shrunk", fullyShrunk)

                if (fullyShrunk) {
                    collect("fully shrunk steps", exception.shrinkSteps.bucket(size = 50))
                    collect("fully shrunk args", shrunkArgs.toString())
                } else {
                    exceptionsWithBadShrinks.add(exception)
                }

                categoriseShrinks(fullyShrunk, originalArgs, shrunkArgs)
            }
        }

        if (exceptionsWithBadShrinks.isNotEmpty()) {
            println("\nSome bad shrinks encountered:")

            exceptionsWithBadShrinks
                .sortedBy { it.smallestResult.input.toString().length }
                .take(5)
                .forEach { println(it.asBadShrinkExample()) }
        }

        counter.checkPercentages("fully shrunk", mapOf(true to minConfidence))
    }

    private fun Int.bucket(size: Int): String {
        val lowerBound = (this / size) * size
        val upperBound = lowerBound + size
        return "$lowerBound-$upperBound"
    }

    private fun PropertyFalsifiedException.asBadShrinkExample(): String {
        val shortenedOriginalInput = originalResult.input.toString().let {
            if (it.length > 100) it.take(100) + " (remaining args truncated)" else it
        }

        return """
            |----
            |Seed: $seed
            |Iteration: $iteration
            |Original args: $shortenedOriginalInput
            |Shrunk args: ${smallestResult.input}
            |Shrink steps: $shrinkSteps
            """.trimMargin()
    }
}
