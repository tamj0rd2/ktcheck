package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.ConstantGeneratorContract
import org.junit.jupiter.api.Disabled

internal class ConstantGeneratorTest : BaseContractImpl(), ConstantGeneratorContract {
    @Disabled("constant generator doesn't support edge cases")
    override fun `edge cases and their shrinks are reproducible via their returned tree`() {
        super.`edge cases and their shrinks are reproducible via their returned tree`()
    }
}
