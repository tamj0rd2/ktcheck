package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.DistinctListGeneratorContract
import org.junit.jupiter.api.Disabled

internal class DistinctListGeneratorTest : BaseContractImpl(), DistinctListGeneratorContract {
    @Disabled("todo: make this pass")
    override fun `edge cases and their shrinks are reproducible via their returned tree`() {
        super.`edge cases and their shrinks are reproducible via their returned tree`()
    }

    @Disabled("todo: make this pass")
    override fun `shrinks a list of 1 element`() {
        super.`shrinks a list of 1 element`()
    }

    @Disabled("todo: make this pass")
    override fun `shrinks a list of 2 elements`() {
        super.`shrinks a list of 2 elements`()
    }

    @Disabled("todo: make this pass")
    override fun `can shrink lists with a minimum size greater than 0`() {
        super.`can shrink lists with a minimum size greater than 0`()
    }

    @Disabled("todo: make this pass")
    override fun `shrinks to empty list when list is not empty`() {
        super.`shrinks to empty list when list is not empty`()
    }

    @Disabled("todo: make this pass")
    override fun `all shrunk element values are within the generator range`() {
        super.`all shrunk element values are within the generator range`()
    }
}
