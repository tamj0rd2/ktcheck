@file:Suppress("unused")

package com.tamj0rd2.ktcheck.util

sealed interface Tuple {
    val values: List<Any?>
}

@ConsistentCopyVisibility
data class Tuple2<A, B> internal constructor(val val1: A, val val2: B) : Tuple {
    override val values = listOf(val1, val2)

    operator fun <Value> plus(value: Value) = Tuple3(val1, val2, value)
}

@ConsistentCopyVisibility
data class Tuple3<A, B, C> internal constructor(val val1: A, val val2: B, val val3: C) : Tuple {
    override val values = listOf(val1, val2, val3)

    operator fun <Value> plus(value: Value) = Tuple4(val1, val2, val3, value)
}

@ConsistentCopyVisibility
data class Tuple4<A, B, C, D> internal constructor(val val1: A, val val2: B, val val3: C, val val4: D) : Tuple {
    override val values = listOf(val1, val2, val3, val4)

    operator fun <Value> plus(value: Value) = Tuple5(val1, val2, val3, val4, value)
}

@ConsistentCopyVisibility
data class Tuple5<A, B, C, D, E> internal constructor(val val1: A, val val2: B, val val3: C, val val4: D, val val5: E) : Tuple {
    override val values = listOf(val1, val2, val3, val4, val5)
}
