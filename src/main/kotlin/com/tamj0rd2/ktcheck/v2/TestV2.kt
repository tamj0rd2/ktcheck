package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.HardcodedTestConfig
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.ShrinkingConstraint
import com.tamj0rd2.ktcheck.Test
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.TestFailure
import com.tamj0rd2.ktcheck.core.RandomTree

@OptIn(HardcodedTestConfig::class)
internal fun <T> test(config: TestConfig, gen: GenV2<T>, test: Test<T>) {
    val edgeCases = gen.edgeCases()

    fun runIteration(iteration: Int) {
        val input = if (iteration <= edgeCases.size) {
            edgeCases.elementAt(iteration - 1)
        } else {
            gen.generate(RandomTree.new(config.seed.next(iteration)))
        }

        val testFailure = test.test(input.value) ?: return

        val shrinkTracker = ShrinkTracker<T>(
            printSteps = config.printShrinkSteps,
            shrinkingConstraint = config.shrinkingConstraint.apply { onStart() }
        )

        val shrunkResult = test.getSmallestCounterExample(
            smallestSoFar = testFailure,
            candidates = input.shrinks.iterator(),
            tracker = shrinkTracker,
        )

        PropertyFalsifiedException(
            seed = config.seed.value,
            iteration = iteration,
            original = testFailure,
            shrunk = shrunkResult.takeIf { it.input != testFailure.input },
            shrinkSteps = shrinkTracker.shrinkSteps
        ).also {
            config.reporter.reportFailure(it)
            throw it
        }
    }

    val startingIteration = (config.replayIteration ?: 1)
    (startingIteration..<startingIteration + config.iterations).forEach(::runIteration)
    config.reporter.reportSuccess(config.iterations)
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

    fun recordShrinkAttempt(input: T): Boolean {
        if (!seenValues.add(input)) return false

        shrinkingConstraint.onStep()
        shrinkSteps += 1

        if (printSteps) println("step ${shrinkSteps}: $input")
        return true
    }
}

private tailrec fun <T> Test<T>.getSmallestCounterExample(
    smallestSoFar: TestFailure<T>,
    candidates: Iterator<GenResultV2<T>>,
    tracker: ShrinkTracker<T>,
): TestFailure<T> {
    if (!candidates.hasNext() || tracker.shouldStopShrinking()) return smallestSoFar

    val (shrunkInput, newInputShrinks) = candidates.next()
    val testResult = test(shrunkInput)

    if (!tracker.recordShrinkAttempt(shrunkInput)) {
        return getSmallestCounterExample(smallestSoFar, candidates, tracker)
    }

    return if (testResult is TestFailure) {
        getSmallestCounterExample(testResult, newInputShrinks.iterator(), tracker)
    } else {
        getSmallestCounterExample(smallestSoFar, candidates, tracker)
    }
}
