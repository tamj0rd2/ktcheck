package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.Property
import com.tamj0rd2.ktcheck.PropertyFalsifiedException
import com.tamj0rd2.ktcheck.TestConfig
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow

internal fun <T> test(config: TestConfig, gen: GenImpl<T>, property: Property<T>) {
    TestRunner(config = config, gen = gen, property = property).run()
}

private class TestRunner<T>(
    private val config: TestConfig,
    private val gen: GenImpl<T>,
    private val property: Property<T>,
) {
    private val edgeCases = gen.edgeCases(RandomTree.forEdgeCases)

    fun run() {
        val startingIteration = (config.replayIteration ?: 1)
        repeat(config.iterations) {
            val iteration = startingIteration + it

            val result = runIteration(iteration - 1)
            if (result !is TestIterationResult.DidFalsify<*>) return@repeat

            val exception = PropertyFalsifiedException(
                seed = config.seed.value,
                iteration = iteration,
                original = result.originalFalsification,
                shrunk = result.shrunkFalsification.takeIf { it.input != result.originalFalsification.input },
                shrinkSteps = result.shrinkSteps
            )
            // todo: do I really need a reporter? Could just throw a nicely formatted exception...
            config.reporter.reportFailure(exception)
            throw exception
        }

        config.reporter.reportSuccess(config.iterations)
    }

    private fun runIteration(iterationIdx: Int): TestIterationResult {
        val input = if (iterationIdx in edgeCases.indices) {
            edgeCases.elementAt(iterationIdx)
        } else {
            gen.generate(RandomTree.new(config.seed.next(iterationIdx))).orThrow()
        }

        val originalFalsification = property.test(input.value) ?: return TestIterationResult.DidNotFalsify

        val (shrunkFalsification, shrinkSteps) = findSimplestFalsification(input, originalFalsification)
        return TestIterationResult.DidFalsify(
            originalFalsification = originalFalsification,
            shrunkFalsification = shrunkFalsification,
            shrinkSteps = shrinkSteps
        )
    }

    private fun findSimplestFalsification(
        originalGeneratedValue: GeneratedValue<T>,
        originalFalsification: Property.Falsification<T>,
    ): Pair<Property.Falsification<T>, Int> {
        // todo: shrinkingConstraint is shared. if I wanted iterations to run concurrently, this would break in some way
        //  i'm imagining shrinkingConstraint being a function that returns an auto closable Constraint which would
        //  then allow things to work per-iteration.
        config.shrinkingConstraint.onStart()

        // todo: these 2 values are entirely coupled. They should probably be a single thing.
        var simplestFalsification = originalFalsification
        var candidates = originalGeneratedValue.shrinks.iterator()

        val seenValues = mutableSetOf<T>()

        while (config.shrinkingConstraint.shouldKeepShrinking() && candidates.hasNext()) {
            val shrunkInput = gen.generate(candidates.next()).onFailure { continue }
            if (!seenValues.add(shrunkInput.value)) continue

            config.shrinkingConstraint.onStep()

            val testResult = property.test(shrunkInput.value) ?: continue

            simplestFalsification = testResult
            candidates = shrunkInput.shrinks.iterator()
        }

        return simplestFalsification to seenValues.size
    }

    private sealed interface TestIterationResult {
        data class DidFalsify<T>(
            val originalFalsification: Property.Falsification<T>,
            val shrunkFalsification: Property.Falsification<T>,
            val shrinkSteps: Int,
        ) : TestIterationResult

        data object DidNotFalsify : TestIterationResult
    }
}
