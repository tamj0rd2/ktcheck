package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.IntGeneratorContract
import org.junit.jupiter.api.Test
import strikt.api.expectThrows

internal class IntGeneratorTest : BaseContractImpl(), IntGeneratorContract {
    @Test
    fun `throws if the given value provider would cause the generated value to be out of range`() {
        val gen = int(0..10)
        val tree = tree().withProvider(PredeterminedValueProvider(100))

        expectThrows<IllegalStateException> { gen.generate(tree) }
    }
}
