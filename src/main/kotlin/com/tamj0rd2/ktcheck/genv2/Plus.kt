@file:Suppress("unused")

package com.tamj0rd2.ktcheck.genv2

import com.tamj0rd2.ktcheck.util.Tuple2
import com.tamj0rd2.ktcheck.util.Tuple3
import com.tamj0rd2.ktcheck.util.Tuple4
import com.tamj0rd2.ktcheck.util.Tuple5
import com.tamj0rd2.ktcheck.util.Tuple6
import com.tamj0rd2.ktcheck.util.Tuple7
import com.tamj0rd2.ktcheck.util.Tuple8
import com.tamj0rd2.ktcheck.util.Tuple9
import com.tamj0rd2.ktcheck.util.tuple

@JvmName("zip2")
infix operator fun <T1, T2> Gen<T1>.plus(
    other: Gen<T2>,
) = Gen<Tuple2<T1, T2>> { tree ->
    val (leftValue, leftShrinks) = generate(tree.left)
    val (rightValue, rightShrinks) = other.generate(tree.right)
    val tuple = tuple(leftValue, rightValue)
    GenResult(tuple, combineShrinks(tree, leftShrinks, rightShrinks))
}

@JvmName("zip3")
infix operator fun <T1, T2, T3> Gen<Tuple2<T1, T2>>.plus(
    nextGen: Gen<T3>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip4")
infix operator fun <T1, T2, T3, T4> Gen<Tuple3<T1, T2, T3>>.plus(
    nextGen: Gen<T4>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip5")
infix operator fun <T1, T2, T3, T4, T5> Gen<Tuple4<T1, T2, T3, T4>>.plus(
    nextGen: Gen<T5>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip6")
infix operator fun <T1, T2, T3, T4, T5, T6> Gen<Tuple5<T1, T2, T3, T4, T5>>.plus(
    nextGen: Gen<T6>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip7")
infix operator fun <T1, T2, T3, T4, T5, T6, T7> Gen<Tuple6<T1, T2, T3, T4, T5, T6>>.plus(
    nextGen: Gen<T6>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip8")
infix operator fun <T1, T2, T3, T4, T5, T6, T7, T8> Gen<Tuple7<T1, T2, T3, T4, T5, T6, T7>>.plus(
    nextGen: Gen<T8>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip9")
infix operator fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> Gen<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>.plus(
    nextGen: Gen<T9>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}

@JvmName("zip10")
infix operator fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Gen<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>.plus(
    nextGen: Gen<T10>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    GenResult(
        value = tupleValue + nextValue,
        shrinks = combineShrinks(tree, tupleShrinks, nextShrinks)
    )
}
