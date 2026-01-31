package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.v2.GenV2

sealed interface Property<T> {
    fun test(input: T): Falsification<T>?

    data class Falsification<T>(val input: T, val error: AssertionError?)
}

fun interface BooleanProperty<T> : Property<T> {
    /** Runs the test on the given input. Should throw an AssertionError if the property fails. */
    operator fun invoke(input: T)

    override fun test(input: T): Property.Falsification<T>? = try {
        invoke(input)
        null
    } catch (e: AssertionError) {
        Property.Falsification(input, e)
    }
}

fun interface ThrowingProperty<T> : Property<T> {
    /** Runs the test on the given input. Should return false is the property fails */
    operator fun invoke(input: T): Boolean

    override fun test(input: T): Property.Falsification<T>? =
        if (invoke(input)) null else Property.Falsification(input, null)
}

@Suppress("unused")
fun <T> forAll(gen: Gen<T>, property: ThrowingProperty<T>) = forAll(TestConfig(), gen, property)

fun <T> forAll(config: TestConfig, gen: Gen<T>, property: ThrowingProperty<T>) =
    runPropertyTest(config, gen, property as Property<T>)

@Suppress("unused")
fun <T> checkAll(gen: Gen<T>, property: BooleanProperty<T>) = checkAll(TestConfig(), gen, property)

fun <T> checkAll(config: TestConfig, gen: Gen<T>, property: BooleanProperty<T>) =
    runPropertyTest(config, gen, property as Property<T>)

private fun <T> runPropertyTest(config: TestConfig, gen: Gen<T>, property: Property<T>) {
    when (gen) {
        is GenV2 -> com.tamj0rd2.ktcheck.v2.test(config, gen, property)
        else -> throw IllegalArgumentException("Unsupported Gen implementation: ${gen::class}")
    }
}

class PropertyFalsifiedException internal constructor(
    val seed: Long,
    val iteration: Int,
    val original: Property.Falsification<*>,
    val shrunk: Property.Falsification<*>?,
    val shrinkSteps: Int,
) : AssertionError("Property falsified") {
    internal val smallest = shrunk ?: original
    override val cause = smallest.error
}
