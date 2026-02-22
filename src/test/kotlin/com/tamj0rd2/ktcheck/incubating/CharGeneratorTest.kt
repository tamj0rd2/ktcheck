package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.CharGeneratorContract
import org.junit.jupiter.api.Disabled

internal class CharGeneratorTest : BaseContractImpl(), CharGeneratorContract {
    @Disabled("todo: make this test pass asap")
    override fun `edge cases and their shrinks are reproducible via their returned tree`() {
        super.`edge cases and their shrinks are reproducible via their returned tree`()
    }
}
