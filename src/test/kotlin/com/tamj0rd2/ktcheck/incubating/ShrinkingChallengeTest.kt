package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.ShrinkingChallengeContract
import org.junit.jupiter.api.Assumptions

internal class ShrinkingChallengeTest : BaseContractImpl(), ShrinkingChallengeContract {
    override fun deletion() {
        Assumptions.assumeTrue(false, "TODO: make this pass and delete this override")
    }

    override fun `difference must not be one`() {
        Assumptions.assumeTrue(false, "TODO: make this pass and delete this override")
    }

    override fun `difference must not be small`() {
        Assumptions.assumeTrue(false, "TODO: make this pass and delete this override")
    }

    override fun `difference must not be zero`() {
        Assumptions.assumeTrue(false, "TODO: make this pass and delete this override")
    }
}
