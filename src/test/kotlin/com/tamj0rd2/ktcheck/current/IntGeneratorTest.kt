package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.IntGeneratorContract
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Seed
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isIn

internal class IntGeneratorTest : BaseContractImpl(), IntGeneratorContract {
    @Test
    fun `falls back to random generation if the predetermined value falls outside of the generator's range`() {
        val gen = int(0..10)
        val seed = Seed.random()
        val tree = tree(seed).withProvider(PredeterminedValueProvider(100, tree(seed).provider))
        expectThat(gen.generate(tree)).value.isIn(0..10)
    }
}
