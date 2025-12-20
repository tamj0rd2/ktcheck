package com.tamj0rd2.ktcheck.genv2

import org.junit.jupiter.api.Test

class ListTest {
    @Test
    fun `can generate a long list`() {
        val gen = Gen.constant(1).list(10_000)
        gen.sample()
    }
}
