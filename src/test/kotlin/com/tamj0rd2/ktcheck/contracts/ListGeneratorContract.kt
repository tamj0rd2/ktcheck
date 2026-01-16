package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThan
import strikt.assertions.size

internal interface ListGeneratorContract : BaseContract {
    @Test
    fun `can generate a long list without stack overflow`() {
        constant(1).list(10_000).sample()
    }

    @Test
    fun `shrinks a list of 1 element`() {
        val gen = int(0..4).list()

        val tree = producerTree {
            left(1)
            right {
                left(4)
            }
        }

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(listOf(4))

        expectThat(result.shrunkValues).isEqualTo(
            listOf(
                // shrinks the size
                emptyList(),
                // shrinks the value
                listOf(0),
                listOf(2),
                listOf(3),
            )
        )
    }

    @Test
    fun `recursively shrinks a list of 2 elements`() {
        val gen = int(0..4).list()
        val tree = producerTree {
            left(2)
            right {
                left(3)
                right {
                    left(4)
                }
            }
        }

        val rootResult = gen.generate(tree)
        expectThat(rootResult.value).isEqualTo(listOf(3, 4))

        val distinctShinks = rootResult.deeplyShrunkValues.toList().distinct()
        expectThat(distinctShinks).contains(
            // size shrinks
            emptyList(),
            listOf(3),
            listOf(4),
            // element shrinks
            listOf(0, 4),
            listOf(3, 0),
            listOf(0, 0),
        )
    }

    @Test
    fun `once the elements of a list have been shrunk, the resultant shrinks can also be shrunk by size`() {
        val gen = int(0..10).list(0..3)
        val tree = producerTree {
            left(2) // size
            right {
                left(3)
                right {
                    left(4)
                }
            }
        }

        val root = gen.generate(tree)
        expectThat(root.value).isEqualTo(listOf(3, 4))

        val firstNonSizeShrink = root.shrinks.first { it.value.size == root.value.size }
        expectThat(firstNonSizeShrink.shrinks.toList())
            .describedAs("shrinks of ${firstNonSizeShrink.value}")
            .any { get { value }.size.isLessThan(root.value.size) }
    }
}
