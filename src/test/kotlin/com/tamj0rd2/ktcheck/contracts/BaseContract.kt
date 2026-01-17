package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.NoOpTestReporter
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.treeWhere
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.trees
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.forAll
import com.tamj0rd2.ktcheck.v2.GenResultV2
import com.tamj0rd2.ktcheck.v2.GenV2
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isNotNull
import java.time.Duration

internal interface BaseContract : GenFacade

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

internal fun <T> Gen<T>.generate(tree: ProducerTree = ProducerTree.new()): GenResults<T> = when (this) {
    is GenV2 -> {
        val (value, shrinks) = generate(tree)
        GenResults(value, collectShrinksRecursively(shrinks))
    }

    else -> error("Unsupported Gen type: ${this::class}")
}

private fun <T> GenV2<T>.collectShrinksRecursively(shrinks: Sequence<GenResultV2<T>>): Sequence<GenResults<T>> =
    sequence {
        for (shrink in shrinks) {
            yield(
                GenResults(
                    value = shrink.value,
                    shrinks = collectShrinksRecursively(shrink.shrinks)
                )
            )
        }
    }

internal fun <T> Gen<T>.sequence(): Sequence<GenResults<T>> =
    trees().map { generate(it) }

/** Retries generations until the exact [value] is produced. */
internal fun <T> Gen<T>.generating(value: T): GenResults<T> =
    generating { it == value }

/** Retries generations until some value satisfying [predicate] is produced. */
internal fun <T> Gen<T>.generating(predicate: (T) -> Boolean): GenResults<T> =
    generate(findTreeProducing(Seed.random(), predicate))

internal fun <T> Gen<T>.findTreeProducing(value: T, seed: Seed = Seed.random()): ProducerTree =
    findTreeProducing(seed) { it == value }

internal fun <T> Gen<T>.findTreeProducing(seed: Seed = Seed.random(), predicate: (T) -> Boolean): ProducerTree =
    treeWhere(seed) { predicate(generate(it).value) }

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
