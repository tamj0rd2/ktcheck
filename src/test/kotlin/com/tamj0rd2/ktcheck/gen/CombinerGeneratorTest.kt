package com.tamj0rd2.ktcheck.gen

import com.tamj0rd2.ktcheck.gen.GenTests.Companion.generateWithShrunkValues
import com.tamj0rd2.ktcheck.producer.ProducerTreeDsl.Companion.producerTree
import com.tamj0rd2.ktcheck.producer.Seed
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasLength
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.startsWith

class CombinerGeneratorTest {
    @Test
    fun `generators using the builder shrink correctly`() {
        data class Person(val name: String, val age: Int)

        val gen = Gen.combine {
            Person(
                name = Gen.char('a'..'d').string(1..5).bind(),
                age = Gen.int(0..150).bind()
            )
        }

        val tree = producerTree(Seed(1)) {
            // used for first gen - name
            left {
                left(4) // name length
                right {
                    left(3) // index 3 of chars - 'd'
                }
            }
            // used for second gen - age
            right {
                left(28) // age
            }
        }

        val (value, shrunkValues) = gen.generateWithShrunkValues(tree)

        expectThat(value) {
            get { name }.hasLength(4).startsWith('d')
            get { age }.isEqualTo(28)
        }

        expectThat(shrunkValues)
            .isNotEmpty()
            .any { get { name }.isEqualTo("d") }
            .any { get { age }.isEqualTo(0) }
    }
}
