package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.HardcodedTestConfig
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
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
                val (shrunkResult, shrinkSteps) = testResultsGen.getSmallestCounterExample(
                    testResult,
                    shrinks.iterator()
                )

                PropertyFalsifiedException(
                    seed = config.seed.value,
                    iteration = iteration,
                    originalResult = testResult,
                    shrunkResult = shrunkResult.takeIf { it.input != testResult.input },
                    shrinkSteps = shrinkSteps
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

private tailrec fun <T> GenV2<TestResult<T>>.getSmallestCounterExample(
    testResult: TestResult.Failure<T>,
    iterator: Iterator<GenResultV2<TestResult<T>>>,
    steps: Int = 0,
): Pair<TestResult.Failure<T>, Int> {
    if (!iterator.hasNext()) return testResult to steps

    val (shrunkTestResult, newShrinks) = iterator.next()

    return if (shrunkTestResult is TestResult.Failure) {
        getSmallestCounterExample(shrunkTestResult, newShrinks.iterator(), steps + 1)
    } else {
        getSmallestCounterExample(testResult, iterator, steps + 1)
    }
}
