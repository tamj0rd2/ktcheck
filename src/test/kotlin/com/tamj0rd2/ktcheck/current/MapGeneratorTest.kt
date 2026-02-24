package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.MapGeneratorContract
import org.junit.jupiter.api.Disabled

internal class MapGeneratorTest : BaseContractImpl(), MapGeneratorContract {
    @Disabled("TODO: If I decide to keep this implementation, I need to fix this test")
    override fun `propagates mapped versions of the underlying edge cases and their shrinks`() {
        super.`propagates mapped versions of the underlying edge cases and their shrinks`()
    }
}
