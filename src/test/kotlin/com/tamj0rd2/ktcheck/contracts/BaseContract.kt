package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.NoOpTestReporter
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.forAll
import org.junit.jupiter.api.assertTimeoutPreemptively
import strikt.api.expectThrows
import strikt.assertions.isNotNull
import java.time.Duration

internal interface BaseContract : GenFacade {
    fun <T> Gen<T>.generate(tree: ProducerTree): T

    fun <T> Gen<T>.generateWithShrunkValues(tree: ProducerTree): Pair<T, List<T>>

    fun <T> Gen<T>.generateWithDeepShrinks(tree: ProducerTree): Pair<T, Sequence<T>>

    fun <T> Gen<T>.expectGenerationAndShrinkingToEventuallyComplete(shrunkValueRequired: Boolean = true) {
        var shrinksBeforeTimeout = -1
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(1), "Shrinking took too long") {
                val ex = expectThrows<PropertyFalsifiedException> {
                    forAll(TestConfig().withReporter(NoOpTestReporter), this) {
                        shrinksBeforeTimeout += 1
                        false
                    }
                }

                if (shrunkValueRequired) {
                    ex.get { shrunkResult }.isNotNull()
                }
            }
        } catch (e: Throwable) {
            println("managed $shrinksBeforeTimeout shrinks before exploding")
            throw e
        }
    }
}
