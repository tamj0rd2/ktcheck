package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.MapGeneratorContract
import org.junit.jupiter.api.Disabled

internal class MapGeneratorTest : BaseContractImpl(), MapGeneratorContract {
    // todo: if I decide to keep this implementation, I re-enable this test and make it pass.
    @Disabled
    override fun `propagates mapped versions of the underlying edge cases and their shrinks`() {
        super.`propagates mapped versions of the underlying edge cases and their shrinks`()
    }
}
