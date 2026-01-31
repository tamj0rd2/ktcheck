package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.v2.GenV2

data class TestFailure<T>(val input: T, val error: AssertionError?)

sealed interface Test<T> {
    fun test(input: T): TestFailure<T>?
}

fun interface TestByThrowing<T> : Test<T> {
    /** Runs the test on the given input. Should throw an AssertionError if the test fails. */
    operator fun invoke(input: T)

    override fun test(input: T): TestFailure<T>? = try {
        invoke(input)
        null
    } catch (e: AssertionError) {
        TestFailure(input, e)
    }
}

fun interface TestByBool<T> : Test<T> {
    /** Runs the test on the given input. Should throw an AssertionError if the test fails. */
    operator fun invoke(input: T): Boolean

    override fun test(input: T): TestFailure<T>? =
        if (invoke(input)) null else TestFailure(input, null)
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

class PropertyFalsifiedException internal constructor(
    val seed: Long,
    val iteration: Int,
    val original: TestFailure<*>,
    val shrunk: TestFailure<*>?,
    val shrinkSteps: Int,
) : AssertionError("Property falsified") {
    internal val smallest = shrunk ?: original
    override val cause = smallest.error
}
