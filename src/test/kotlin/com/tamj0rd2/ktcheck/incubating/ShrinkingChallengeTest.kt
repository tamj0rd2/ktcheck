package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.ShrinkingChallengeContract
import org.junit.jupiter.api.Disabled

internal class ShrinkingChallengeTest : BaseContractImpl(), ShrinkingChallengeContract {
    @Disabled("TODO: make this pass and delete this override")
    override fun `difference must not be one`() {
        super.`difference must not be one`()
    }

    @Disabled("TODO: make this pass and delete this override")
    override fun `difference must not be small`() {
        super.`difference must not be small`()
    }

    @Disabled("TODO: make this pass and delete this override")
    override fun `difference must not be zero`() {
        super.`difference must not be zero`()
    }
}
