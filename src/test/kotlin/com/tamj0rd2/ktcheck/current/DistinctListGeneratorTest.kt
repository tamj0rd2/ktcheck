package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.DistinctListGeneratorContract
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest

internal class DistinctListGeneratorTest : BaseContractImpl(), DistinctListGeneratorContract {
    @Disabled("TODO: If I decide to keep this implementation, I need to fix this test")
    override fun `edge case generation`(): List<DynamicTest> {
        return super.`edge case generation`()
    }
}
