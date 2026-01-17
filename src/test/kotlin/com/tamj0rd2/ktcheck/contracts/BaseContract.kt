package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.NoOpTestReporter
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.trees
import com.tamj0rd2.ktcheck.forAll
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isNotNull
import java.time.Duration

internal class GenResults<T>(
    val value: T,
    val shrinks: Sequence<GenResults<T>>,
) {
    override fun toString(): String {
        return "GenResults(value=$value)"
    }

    val shrunkValues get() = shrinks.map { it.value }.toList()

    val deeplyShrunkValues: Sequence<T>
        get() = sequence {
            for (shrink in shrinks) {
                yield(shrink.value)
                yieldAll(shrink.deeplyShrunkValues)
            }
        }
}

internal interface BaseContract : GenFacade {
    fun <T> Gen<T>.generate(tree: ProducerTree = ProducerTree.new()): GenResults<T>

    fun <T> Gen<T>.sequence(): Sequence<GenResults<T>> =
        generateSequence { generate(ProducerTree.new()) }

    /** Retries generations until the exact [value] is produced. */
    fun <T> Gen<T>.generating(value: T): GenResults<T> =
        generating { it == value }

    /** Retries generations until some value satisfying [predicate] is produced. */
    fun <T> Gen<T>.generating(predicate: (T) -> Boolean): GenResults<T> =
        generate(trees().first { produces(it, predicate) })

    /** Checks whether the given [tree] produces a value satisfying the [predicate]. */
    fun <T> Gen<T>.produces(tree: ProducerTree, predicate: (T) -> Boolean): Boolean =
        predicate(generate(tree).value)

    fun <T> Gen<T>.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired: Boolean = true) {
        var shrinksBeforeTimeout = -1
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(1), "Shrinking took too long") {
                val ex = try {
                    forAll(TestConfig().withReporter(NoOpTestReporter), this) {
                        shrinksBeforeTimeout += 1
                        false
                    }
                    fail("Expected property to be falsified")
                } catch (e: PropertyFalsifiedException) {
                    e
                }

                if (shrunkValueRequired) {
                    expectThat(ex).get { shrunkResult }.isNotNull()
                }
            }
        } catch (e: Throwable) {
            println("managed $shrinksBeforeTimeout shrinks before exploding")
            throw e
        }
    }
}
