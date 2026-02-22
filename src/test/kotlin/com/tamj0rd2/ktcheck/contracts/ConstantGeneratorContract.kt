package com.tamj0rd2.ktcheck.contracts

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

internal interface ConstantGeneratorContract : BaseContract {
    override val exampleGen get() = constant("hello")
    override val genShouldHaveEdgeCases get() = false

    @Test
    fun `constant always produces the same value and doesn't shrink`() {
        val gen = constant(10)
        repeatTest {
            val result = gen.generate(tree())
            expectThat(result.value).isEqualTo(10)
            expectThat(result).shrunkValues.isEmpty()
            expectThat(gen.edgeCases()).isEmpty()
        }
    }
}
