package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.v2.GenV2

sealed interface Test<T> {
    fun test(input: T): AssertionError?
}

fun interface TestByThrowing<T> : Test<T> {
    /** Runs the test on the given input. Should throw an AssertionError if the test fails. */
    operator fun invoke(input: T)

    override fun test(input: T): AssertionError? = try {
        invoke(input)
        null
    } catch (e: AssertionError) {
        e
    }
}

fun interface TestByBool<T> : Test<T> {
    /** Runs the test on the given input. Should throw an AssertionError if the test fails. */
    operator fun invoke(input: T): Boolean

    override fun test(input: T): AssertionError? =
        if (invoke(input)) null else AssertionError("Test falsified")
}

@Suppress("unused")
fun <T> forAll(gen: Gen<T>, test: TestByBool<T>) = forAll(TestConfig(), gen, test)
fun <T> forAll(config: TestConfig, gen: Gen<T>, test: TestByBool<T>) = test(config, gen, test as Test<T>)

@Suppress("unused")
fun <T> checkAll(gen: Gen<T>, test: TestByThrowing<T>) = checkAll(TestConfig(), gen, test)
fun <T> checkAll(config: TestConfig, gen: Gen<T>, test: TestByThrowing<T>) = test(config, gen, test as Test<T>)

private fun <T> test(config: TestConfig, gen: Gen<T>, test: Test<T>) {
    when (gen) {
        is GenV2 -> com.tamj0rd2.ktcheck.v2.test(config, gen, test)
        else -> throw IllegalArgumentException("Unsupported Gen implementation: ${gen::class}")
    }
}

// todo: I wish this all lived inside of TestResult. having an extra things seems... extra
class PropertyFalsifiedException(
    val seed: Long,
    val iteration: Int,
    val originalResult: TestResult.Failure<*>,
    val shrunkResult: TestResult.Failure<*>?,
    val shrinkSteps: Int,
) : AssertionError("Property falsified") {
    internal val smallestResult = shrunkResult ?: originalResult
    override val cause: Throwable = smallestResult.failure
}
