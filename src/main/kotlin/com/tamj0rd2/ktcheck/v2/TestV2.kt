package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.HardcodedTestConfig
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.ShrinkingConstraint
import com.tamj0rd2.ktcheck.Test
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.TestResult
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.v2.GenV2.Companion.map

@OptIn(HardcodedTestConfig::class)
internal fun <T> test(config: TestConfig, gen: GenV2<T>, test: Test<T>) {
    // todo: intellisense is very bad when working on the project. 3 separate imports for map...
    val testResultsGen = gen.map { test.getResultFor(it) }

    fun runIteration(iteration: Int) {
        val sampleTree = ProducerTree.new(config.seed.next(iteration))
        val (testResult, shrinks) = (testResultsGen as GenV2).generate(sampleTree)

        when (testResult) {
            is TestResult.Success -> return

            is TestResult.Failure -> {
                val tracker = ShrinkTracker<T>(
                    printSteps = config.printShrinkSteps,
                    shrinkingConstraint = config.shrinkingConstraint.apply { onStart() }
                )

                val shrunkResult = testResultsGen.getSmallestCounterExample(
                    testResult = testResult,
                    iterator = shrinks.iterator(),
                    tracker = tracker,
                )

                PropertyFalsifiedException(
                    seed = config.seed.value,
                    iteration = iteration,
                    originalResult = testResult,
                    shrunkResult = shrunkResult.takeIf { it.input != testResult.input },
                    shrinkSteps = tracker.shrinkSteps
                ).also {
                    config.reporter.reportFailure(it)
                    throw it
                }
            }
        }
    }

    val startingIteration = (config.replayIteration ?: 1)
    (startingIteration..<startingIteration + config.iterations).forEach(::runIteration)
    config.reporter.reportSuccess(config.iterations)
}

private fun <T> Test<T>.getResultFor(t: T): TestResult<T> {
    val failure = test(t) ?: return TestResult.Success(t)
    return TestResult.Failure(t, failure)
}

private class ShrinkTracker<T>(
    private val printSteps: Boolean,
    private val shrinkingConstraint: ShrinkingConstraint,
) {
    var shrinkSteps: Int = 0
        private set

    private val seenValues = mutableSetOf<T>()

    fun shouldStopShrinking(): Boolean =
        shrinkingConstraint.shouldStopShrinking()

    fun recordShrinkAttempt(result: TestResult<T>): Boolean {
        if (!seenValues.add(result.input)) {
            return false
        }

        shrinkingConstraint.onStep()
        shrinkSteps += 1

        if (printSteps) {
            val suffix = if (result is TestResult.Failure) "(falsified)" else "(succeeded)"
            println("step ${shrinkSteps}: ${result.input} $suffix")
        }

        return true
    }
}

private tailrec fun <T> GenV2<TestResult<T>>.getSmallestCounterExample(
    testResult: TestResult.Failure<T>,
    iterator: Iterator<GenResultV2<TestResult<T>>>,
    tracker: ShrinkTracker<T>,
): TestResult.Failure<T> {
    if (!iterator.hasNext() || tracker.shouldStopShrinking()) return testResult

    val (shrunkTestResult, newShrinks) = iterator.next()

    if (!tracker.recordShrinkAttempt(shrunkTestResult)) {
        return getSmallestCounterExample(testResult, iterator, tracker)
    }

    return if (shrunkTestResult is TestResult.Failure) {
        getSmallestCounterExample(shrunkTestResult, newShrinks.iterator(), tracker)
    } else {
        getSmallestCounterExample(testResult, iterator, tracker)
    }
}
