package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.producerTrees
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.doesNotContain
import strikt.assertions.first
import strikt.assertions.isContainedIn
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThan
import strikt.assertions.isNotEmpty

// todo: most these tests could be made more generic and framed around the oneOfValues generator
internal interface CharGeneratorContract : BaseContract {
    @TestFactory
    fun `can generate a character from a collection`(): List<DynamicTest> {
        val testCases = mapOf(
            "single lowercase char" to listOf('a'),
            "single uppercase char" to listOf('Z'),
            "single digit" to listOf('5'),
            "single special char" to listOf('!'),
            "lowercase range" to ('a'..'z'),
            "uppercase range" to ('A'..'Z'),
            "digit range" to ('0'..'9'),
            "mixed chars" to listOf('a', 'B', '3', '!', ' '),
            "special chars" to listOf('!', '@', '#', '$', '%'),
        )

        return testCases.map { (desc, chars) ->
            DynamicTest.dynamicTest(desc) {
                char(chars)
                    .samples()
                    .take(1000)
                    .forEach { expectThat(it).isContainedIn(chars) }
            }
        }
    }

    @Test
    fun `generates a variety of characters over multiple runs`() {
        withCounter {
            char(('a'..'z').toList()).samples().take(100000).forEach { value ->
                collect(
                    when (value) {
                        in 'a'..'i' -> "a-i"
                        in 'j'..'r' -> "j-r"
                        in 's'..'z' -> "s-z"
                        else -> "other"
                    }
                )
            }
        }.checkPercentages(
            mapOf(
                "a-i" to 30.0,
                "j-r" to 30.0,
                "s-z" to 30.0
            )
        )
    }

    @Test
    fun `using the same seed generates the same values`() {
        val seed = 12345L
        val gen = char(('a'..'z').toList())
        val firstRun = gen.samples(seed).take(100).toList()
        val secondRun = gen.samples(seed).take(100).toList()
        expectThat(secondRun).isEqualTo(firstRun)
    }


    @Test
    fun `shrinks toward the smallest character, smallest first`() {
        val chars = 'a'..'d'
        val gen = char(chars)
        val tree = ProducerTree.new().withValue(chars.indexOf('d'))

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo('d')
        expectThat(result.shrunkValues).isEqualTo(listOf('a', 'c'))
    }

    @Test
    fun `if the smallest character was generated, it won't yield any shrinks`() {
        val chars = 'a'..'d'
        val gen = char(chars)
        val tree = ProducerTree.new().withValue(chars.indexOf('a'))

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo('a')
        expectThat(result.shrunkValues).isEmpty()
    }

    @Test
    fun `shrinks for the non-lowest character always yield the smallest character`() {
        val chars = ('a'..'z').toList()
        val gen = char(chars)

        producerTrees().map { gen.generate(it) }
            .filter { it.value != chars.first() }
            .take(100)
            .forEach { expectThat(it.shrunkValues).first().isEqualTo(chars.first()) }
    }

    @Test
    fun `the original generated character is not included in shrinks`() {
        val chars = ('a'..'z').toList()
        val gen = char(chars)

        producerTrees().map { gen.generate(it) }
            .take(100)
            .forEach { expectThat(it.shrunkValues).doesNotContain(it.value) }
    }

    @Test
    fun `shrinks are closer to the lowest character than the original generated character`() {
        val chars = ('a'..'z').toList()
        val gen = char(chars)
        val lowestChar = chars.first()

        producerTrees().map { gen.generate(it) }
            .filter { it.value != lowestChar }
            .take(100)
            .forEach {
                val originalIndex = chars.indexOf(it.value)

                expectThat(it.shrunkValues).isNotEmpty().all {
                    get { chars.indexOf(this) }
                        .describedAs("shrunk index (closer to lowest)")
                        .isLessThan(originalIndex)
                }
            }
    }
}
