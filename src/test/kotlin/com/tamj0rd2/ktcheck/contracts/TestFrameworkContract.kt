package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.HardcodedTestConfig
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.ShrinkingConstraint
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.TestReporter
import com.tamj0rd2.ktcheck.checkAll
import com.tamj0rd2.ktcheck.forAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.cause
import strikt.assertions.contains
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import kotlin.time.Duration.Companion.nanoseconds

internal interface TestFrameworkContract : BaseContract {

    @Test
    fun `forAll reports a success if the property holds true`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)


        forAll(testConfig, constant(true)) { it }
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.ReportedSuccess>()
    }

    @Test
    fun `forAll reports a failure if the property is falsified`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)


        expectThrows<AssertionError> { forAll(testConfig, constant(false)) { it } }
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.ReportedFailure>().get { error }
            .isA<AssertionError>()
    }

    @Test
    fun `forAll doesn't do any reporting if an exception is thrown - the error just bubbles up`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)

        val theError = AssertionError("uh oh!")
        expectThrows<AssertionError> { forAll(testConfig, constant(theError)) { throw it } }.isEqualTo(theError)
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.None>()
    }

    @Test
    fun `checkAll reports success if the property doesn't throw`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)


        checkAll(testConfig, constant(null)) { }
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.ReportedSuccess>()
    }

    @Test
    fun `checkAll reports a failure if the property threw an assertion error`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)

        val theError = AssertionError("boom!")
        expectThrows<PropertyFalsifiedException> {
            checkAll(
                testConfig,
                constant(theError)
            ) { throw it }
        }.cause.isEqualTo(theError)
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.ReportedFailure>().get { error }
            .isEqualTo(theError)
    }

    @Test
    fun `checkAll doesn't do any reporting if any other throwable is thrown - it just bubbles up`() {
        val spyTestReporter = SpyTestReporter()
        val testConfig = TestConfig().withReporter(reporter = spyTestReporter)


        class MyThrowable : Throwable()

        val exception = MyThrowable()
        expectThrows<MyThrowable> { checkAll(testConfig, constant(exception)) { throw it } }.isEqualTo(exception)
        expectThat(spyTestReporter.reporting).isA<SpyTestReporter.Reporting.None>()
    }

    @Test
    fun `can constrain the shrinking process with a step limit`() {
        val testConfig = TestConfig().withShrinkingConstraint(ShrinkingConstraint.bySteps(1))

        val exception = expectThrows<PropertyFalsifiedException> {
            forAll(testConfig, int()) { it == 0 }
        }.subject

        expectThat(exception.shrinkSteps).isEqualTo(1)
    }

    @Test
    fun `can constrain the shrinking process with a time limit`() {
        val testConfig = TestConfig().withShrinkingConstraint(ShrinkingConstraint.byDuration(1.nanoseconds))

        val exception = expectThrows<PropertyFalsifiedException> {
            forAll(testConfig, int()) { it == 0 }
        }.subject

        expectThat(exception.shrinkSteps).isEqualTo(0)
    }

    @Test
    fun `can unconstrain the shrinking process with an infinite constraint`() {
        val testConfig = TestConfig().withShrinkingConstraint(ShrinkingConstraint.infinite())

        val exception = expectThrows<PropertyFalsifiedException> {
            forAll(testConfig, int()) { it == 0 }
        }.subject

        expectThat(exception.shrinkSteps).isGreaterThan(0)
    }

    @Test
    fun `includes edge cases during test iterations`() {
        val seenValues = mutableSetOf<Int>()

        forAll(int()) {
            seenValues.add(it)
            true
        }

        expectThat(seenValues).contains(0, 1, -1, Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MAX_VALUE, Int.MAX_VALUE - 1)
    }

    @Test
    @OptIn(HardcodedTestConfig::class)
    fun `can hardcode a specific test iteration to run`() {
        val spyTestReporter = SpyTestReporter()
        val initialConfig = TestConfig().withIterations(100).withReporter(reporter = spyTestReporter)
        val iterationToCheck = (1..100).random()
        val gen = int()


        var iterationCount = 0
        var valueOnSpecifiedIteration: Int? = null

        forAll(initialConfig, gen) {
            iterationCount++
            if (iterationCount == iterationToCheck) valueOnSpecifiedIteration = it
            true
        }

        expectThat(iterationCount).isEqualTo(initialConfig.iterations)
        expectThat(valueOnSpecifiedIteration).isNotNull()

        var replayedIterations = 0
        var valueOnRetry: Int? = null
        val replayConfig = initialConfig.replay(initialConfig.seed.value, iterationToCheck)

        forAll(replayConfig, gen) {
            replayedIterations++
            valueOnRetry = it
            true
        }

        expectThat(replayedIterations).isEqualTo(1)
        expectThat(valueOnRetry).isEqualTo(valueOnSpecifiedIteration)
    }

    private class SpyTestReporter : TestReporter {
        var reporting: Reporting = Reporting.None

        override fun reportSuccess(iterations: Int) {
            reporting = Reporting.ReportedSuccess(iterations)
        }

        override fun reportFailure(exception: PropertyFalsifiedException) {
            reporting = Reporting.ReportedFailure(exception.originalResult.failure)
        }

        sealed interface Reporting {
            data object None : Reporting
            data class ReportedSuccess(val iterations: Int) : Reporting
            data class ReportedFailure(val error: AssertionError) : Reporting
        }
    }
}
