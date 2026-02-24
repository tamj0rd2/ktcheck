package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.CombineWithGeneratorContract
import org.junit.jupiter.api.Disabled

internal class CombineWithGeneratorTest : BaseContractImpl(), CombineWithGeneratorContract {
    // todo: when I decide whether or not to stick with incubating, I should figure out what to do with this.
    @Disabled("TODO: If I decide to keep this implementation, I need to fix this test")
    override fun `combineWith can still produce edge cases for the first generator if the second generator has no edge cases`() {
        super.`combineWith can still produce edge cases for the first generator if the second generator has no edge cases`()
    }

    @Disabled("TODO: If I decide to keep this implementation, I need to fix this test")
    override fun `combineWith can still produce edge cases for the second generator if the first generator has no edge cases`() {
        super.`combineWith can still produce edge cases for the second generator if the first generator has no edge cases`()
    }
}
