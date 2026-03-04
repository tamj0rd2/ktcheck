package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.core.Tuple

sealed interface Property<T> {
    fun test(input: T): Falsification<T>?

    data class Falsification<T>(val input: T, val error: AssertionError?)
}

/** Runs the test on the given input. Should throw an AssertionError if the property fails. */
fun interface ThrowingProperty<T> : Property<T> {
    operator fun invoke(input: T)

    override fun test(input: T): Property.Falsification<T>? = try {
        invoke(input)
        null
    } catch (e: AssertionError) {
        Property.Falsification(input, e)
    }
}

/** Runs the test on the given input. Should return false if the property fails */
fun interface BooleanProperty<T> : Property<T> {
    operator fun invoke(input: T): Boolean

    override fun test(input: T): Property.Falsification<T>? =
        if (invoke(input)) null else Property.Falsification(input, null)
}

fun <T> forAll(gen: Gen<T>, property: BooleanProperty<T>) = forAll(TestConfig(), gen, property)

fun <T> forAll(config: TestConfig, gen: Gen<T>, property: BooleanProperty<T>) =
    runPropertyTest(config, gen, property as Property<T>)

fun <T> checkAll(gen: Gen<T>, property: ThrowingProperty<T>) = checkAll(TestConfig(), gen, property)

fun <T> checkAll(config: TestConfig, gen: Gen<T>, property: ThrowingProperty<T>) =
    runPropertyTest(config, gen, property as Property<T>)

private fun <T> runPropertyTest(config: TestConfig, gen: Gen<T>, property: Property<T>) {
    when (gen) {
        is com.tamj0rd2.ktcheck.current.GenImpl -> com.tamj0rd2.ktcheck.current.test(config, gen, property)
        else -> throw IllegalArgumentException("Unsupported Gen implementation: ${gen::class}")
    }

    config.reportingPrintStream.println("Success: ${config.iterations} iterations succeeded")
}

class PropertyFalsifiedException internal constructor(
    val seed: Long,
    val iteration: Int,
    val original: Property.Falsification<*>,
    val shrunk: Property.Falsification<*>?,
    val shrinkSteps: Int,
) : AssertionError() {
    internal val smallest = shrunk ?: original
    override val cause = smallest.error

    override val message = buildString {
        appendLine("Property falsified on iteration ${iteration}, seed $seed\n")

        if (shrunk != null) {
            appendLine(formatFalsification(prefix = "Shrunk ", result = shrunk))
        } else {
            appendLine("Warning - Could not shrink the input arguments")
        }

        appendLine(formatFalsification(prefix = "Original ", result = original))
    }

    private fun formatFalsification(
        prefix: String,
        result: Property.Falsification<*>,
    ) = buildString {
        appendLine("${prefix}Arguments:")
        appendLine("--------------------")
        when (result.input) {
            is Tuple -> {
                result.input.values.forEachIndexed { index, value ->
                    appendLine("Arg ${index + 1} -> $value")
                }
            }

            else -> appendLine(result.input)
        }

        if (result.error != null) {
            appendLine()
            appendLine("${prefix}Failure:")
            appendLine("--------------------")
            appendLine(result.error)
        }
    }
}
