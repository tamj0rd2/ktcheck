package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.TestFrameworkContract
import org.junit.jupiter.api.Disabled

internal class TestFrameworkTest : BaseContractImpl(), TestFrameworkContract {
    @Disabled("TODO: If I decide to keep this implementation, I need to fix this test")
    override fun `can disable edge cases`() {
        super.`can disable edge cases`()
    }
}
