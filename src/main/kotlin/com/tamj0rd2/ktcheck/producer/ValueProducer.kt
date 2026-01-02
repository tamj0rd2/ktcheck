package com.tamj0rd2.ktcheck.producer

import kotlin.random.Random
import kotlin.random.nextInt

internal sealed interface ValueProducer {
    fun int(range: IntRange): Int
    fun bool(): Boolean
}

@JvmInline
internal value class RandomValueProducer(val seed: Seed) : ValueProducer {
    private val random get() = Random(seed.value)

    override fun int(range: IntRange): Int = random.nextInt(range)

    override fun bool(): Boolean = random.nextBoolean()
}

internal sealed interface Primitive {
    val value: Any?

    @JvmInline
    value class Int(override val value: kotlin.Int) : Primitive

    @JvmInline
    value class Bool(override val value: Boolean) : Primitive
}

@JvmInline
internal value class PredeterminedValue(private val primitive: Primitive) : ValueProducer {
    override fun int(range: IntRange): Int = when {
        primitive !is Primitive.Int -> error("Expected IntChoice but got ${primitive::class.simpleName}")
        primitive.value !in range -> error("IntChoice value ${primitive.value} not in range $range")
        else -> primitive.value
    }

    override fun bool(): Boolean = when (primitive) {
        !is Primitive.Bool -> error("Expected BooleanChoice but got ${primitive::class.simpleName}")
        else -> primitive.value
    }
}
