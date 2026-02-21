package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.contracts.CombinatorGeneratorContract
import org.junit.jupiter.api.Disabled

internal class CombinatorGeneratorTests : BaseContractImpl(), CombinatorGeneratorContract {
    // todo: when I decide whether or not to stick with incubating, I should figure out what to do with this.
    @Disabled
    override fun `combineWith can still produce edge cases for the first generator if the second generator has no edge cases`() {
        super.`combineWith can still produce edge cases for the first generator if the second generator has no edge cases`()
    }

    // todo: when I decide whether or not to stick with incubating, I should figure out what to do with this.
    @Disabled
    override fun `combineWith can still produce edge cases for the second generator if the first generator has no edge cases`() {
        super.`combineWith can still produce edge cases for the second generator if the first generator has no edge cases`()
    }
}
