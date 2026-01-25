package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.core.Seed
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

sealed class TestResult<T> {
    abstract val input: T

    data class Success<T>(override val input: T) : TestResult<T>()

    data class Failure<T>(override val input: T, val failure: AssertionError) : TestResult<T>()
}

@RequiresOptIn(
    message = "Indicates that test configuration has been hardcoded for this test, which should only be done for local debugging purposes.",
    level = RequiresOptIn.Level.WARNING
)
annotation class HardcodedTestConfig

@ConsistentCopyVisibility
data class TestConfig private constructor(
    internal val iterations: Int,
    internal val seed: Seed,
    internal val replayIteration: Int?,
    internal val reporter: TestReporter,
    internal val shrinkingConstraint: ShrinkingConstraint,
    internal val printShrinkSteps: Boolean,
) {
    constructor() : this(
        iterations = System.getProperty(SYSTEM_PROPERTY_TEST_ITERATIONS)?.toIntOrNull() ?: 1000,
        seed = Seed(Random.nextLong()),
        replayIteration = null,
        reporter = PrintingTestReporter(),
        shrinkingConstraint = ShrinkingConstraint.byDuration(1.seconds),
        printShrinkSteps = false,
    )

    fun withIterations(iterations: Int) = copy(iterations = iterations)

    fun withSeed(seed: Long) = copy(seed = Seed(seed))

    fun withShrinkingConstraint(constraint: ShrinkingConstraint) = copy(shrinkingConstraint = constraint)

    @Suppress("unused")
    @Deprecated("I might move this functionality to the Reporter")
    fun printShrinkSteps() = copy(printShrinkSteps = true)

    @HardcodedTestConfig
    fun replay(seed: Long, iteration: Int) = copy(
        iterations = 1,
        seed = Seed(seed),
        replayIteration = iteration
    )

    fun withReporter(reporter: TestReporter) = copy(reporter = reporter)

    companion object {
        internal const val SYSTEM_PROPERTY_TEST_ITERATIONS = "ktcheck.test.iterations"
    }
}

interface ShrinkingConstraint {
    fun onStart() {}
    fun onStep() {}
    fun shouldStopShrinking(): Boolean

    companion object {
        fun infinite(): ShrinkingConstraint = Unconstrained
        fun bySteps(maxSteps: Int): ShrinkingConstraint = ConstrainedBySteps(maxSteps)
        fun byDuration(duration: Duration): ShrinkingConstraint = ConstrainedByDuration(duration)
    }
}

private object Unconstrained : ShrinkingConstraint {
    override fun shouldStopShrinking(): Boolean = false
}

private class ConstrainedByDuration(private val duration: Duration) : ShrinkingConstraint {
    private var mustEndAfter: Instant? = null

    override fun onStart() {
        mustEndAfter = Instant.now().plus(duration.toJavaDuration())
    }

    override fun shouldStopShrinking(): Boolean {
        checkNotNull(mustEndAfter) { "Shrink session not started" }
        return Instant.now().isAfter(mustEndAfter)
    }
}

private class ConstrainedBySteps(private val maxSteps: Int) : ShrinkingConstraint {
    private var stepsTaken = 0

    override fun onStep() {
        stepsTaken += 1
    }

    override fun shouldStopShrinking(): Boolean {
        return stepsTaken >= maxSteps
    }
}
