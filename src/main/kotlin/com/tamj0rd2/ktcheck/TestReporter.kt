package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.core.Tuple
import java.io.PrintStream

interface TestReporter {
    fun reportSuccess(iterations: Int)

    fun reportFailure(exception: PropertyFalsifiedException)
}

object NoOpTestReporter : TestReporter {
    override fun reportSuccess(iterations: Int) {}
    override fun reportFailure(exception: PropertyFalsifiedException) {}
}

class PrintingTestReporter(
    private val printStream: PrintStream = System.out,
    private val showAllDiagnostics: Boolean = true,
) : TestReporter {
    override fun reportSuccess(iterations: Int) {
        printStream.println("Success: $iterations iterations succeeded\n")
    }

    override fun reportFailure(exception: PropertyFalsifiedException) {
        val shrunkFailure = exception.shrunk

        val output = buildString {
            appendLine("Seed: ${exception.seed} - property falsified on iteration ${exception.iteration}\n")

            if (shrunkFailure != null) {
                appendLine(formatFailure(prefix = "Shrunk ", result = shrunkFailure))
            } else {
                appendLine("Warning - Could not shrink the input arguments")
            }

            if (showAllDiagnostics || shrunkFailure == null) {
                appendLine(formatFailure(prefix = "Original ", result = exception.original))
            }
        }

        printStream.println(output)
    }

    private fun formatFailure(prefix: String, result: Property.Falsification<*>): String = buildString {
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
