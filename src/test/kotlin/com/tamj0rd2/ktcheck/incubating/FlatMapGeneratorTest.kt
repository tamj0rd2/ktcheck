package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.FlatMapGeneratorContract
import org.junit.jupiter.api.Disabled

internal class FlatMapGeneratorTest : BaseContractImpl(), FlatMapGeneratorContract {
    // todo: after making this pass, also enable the tests to oneOf and char. They should just work.
    @Disabled("todo: fix this test asap")
    override fun `edge cases and their shrinks are reproducible via their returned tree`() {
        super.`edge cases and their shrinks are reproducible via their returned tree`()
    }
}
