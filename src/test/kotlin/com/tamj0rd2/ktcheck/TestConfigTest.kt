package com.tamj0rd2.ktcheck

import com.tamj0rd2.ktcheck.TestConfig.Companion.SYSTEM_PROPERTY_MAX_SHRINK_STEPS
import com.tamj0rd2.ktcheck.TestConfig.Companion.SYSTEM_PROPERTY_TEST_ITERATIONS
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TestConfigTest {
    @Nested
    inner class Iterations {
        @Test
        fun `defaults to the iterations specified via the system property`() {
            System.setProperty(SYSTEM_PROPERTY_TEST_ITERATIONS, "50")
            expectThat(TestConfig()).get { iterations }.isEqualTo(50)
        }

        @Test
        fun `defaults to 1000 iterations when no value is specified and no system property has been set`() {
            System.clearProperty(SYSTEM_PROPERTY_TEST_ITERATIONS)
            expectThat(TestConfig()).get { iterations }.isEqualTo(1000)
        }

        @Test
        fun `can overwrite the default iterations`() {
            expectThat(TestConfig().withIterations(iterations = 123)).get { iterations }.isEqualTo(123)
        }
    }

    @Nested
    inner class MaxShrinkSteps {
        @Test
        fun `defaults to the max shrink steps specified via the system property`() {
            System.setProperty(SYSTEM_PROPERTY_MAX_SHRINK_STEPS, "123")
            expectThat(TestConfig()).get { maxShrinkSteps }.isEqualTo(123)
        }

        @Test
        fun `defaults to 1000 max shrink steps when no value is specified and no system property has been set`() {
            System.clearProperty(SYSTEM_PROPERTY_MAX_SHRINK_STEPS)
            expectThat(TestConfig()).get { maxShrinkSteps }.isEqualTo(1000)
        }

        @Test
        fun `can overwrite the default max shrink steps`() {
            expectThat(TestConfig().withMaxShrinkSteps(steps = 250)).get { maxShrinkSteps }.isEqualTo(250)
        }
    }
}
