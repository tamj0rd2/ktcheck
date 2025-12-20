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
fun <T1, T2> Gen.Companion.zip(
    gen1: Gen<T1>,
    gen2: Gen<T2>,
) = Gen { tree ->
    val (leftValue, leftShrinks) = gen1.generate(tree.left)
    val (rightValue, rightShrinks) = gen2.generate(tree.right)
    val tuple = tuple(leftValue, rightValue)
    GenResult(tuple, combineShrinks(tree, leftShrinks, rightShrinks))
}

@JvmName("zip3")
fun <T1, T2, T3> Gen.Companion.zip(
    tupleGen: Gen<Tuple2<T1, T2>>,
    nextGen: Gen<T3>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip4")
fun <T1, T2, T3, T4> Gen.Companion.zip(
    tupleGen: Gen<Tuple3<T1, T2, T3>>,
    nextGen: Gen<T4>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip5")
fun <T1, T2, T3, T4, T5> Gen.Companion.zip(
    tupleGen: Gen<Tuple4<T1, T2, T3, T4>>,
    nextGen: Gen<T5>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip6")
fun <T1, T2, T3, T4, T5, T6> Gen.Companion.zip(
    tupleGen: Gen<Tuple5<T1, T2, T3, T4, T5>>,
    nextGen: Gen<T6>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip7")
fun <T1, T2, T3, T4, T5, T6, T7> Gen.Companion.zip(
    tupleGen: Gen<Tuple6<T1, T2, T3, T4, T5, T6>>,
    nextGen: Gen<T6>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip8")
fun <T1, T2, T3, T4, T5, T6, T7, T8> Gen.Companion.zip(
    tupleGen: Gen<Tuple7<T1, T2, T3, T4, T5, T6, T7>>,
    nextGen: Gen<T8>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip9")
fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> Gen.Companion.zip(
    tupleGen: Gen<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>,
    nextGen: Gen<T9>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}

@JvmName("zip10")
fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Gen.Companion.zip(
    tupleGen: Gen<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>,
    nextGen: Gen<T10>,
) = Gen { tree ->
    val (tupleValue, tupleShrinks) = tupleGen.generate(tree.left)
    val (nextValue, nextShrinks) = nextGen.generate(tree.right)
    val tuple = tupleValue + nextValue
    GenResult(tuple, combineShrinks(tree, tupleShrinks, nextShrinks))
}
