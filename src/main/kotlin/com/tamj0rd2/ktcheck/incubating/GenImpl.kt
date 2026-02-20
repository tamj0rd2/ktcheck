package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.core.Seed
import kotlin.reflect.KClass

internal sealed class GenImpl<T> : Gen<T> {
    abstract fun generate(tree: RandomTree): GenResultV2<T>

    abstract fun edgeCases(): List<GenResultV2<T>>

    override fun sample(seed: Long) = generate(RandomTree.new(Seed(seed))).value

    override fun <R> map(fn: (T) -> R) = MapGen(this, fn)

    @Suppress("UNCHECKED_CAST")
    override fun <R> flatMap(fn: (T) -> Gen<R>) = FlatMapGen(this, fn as (T) -> GenImpl<R>)

    override fun <T2, R> combineWith(
        nextGen: Gen<T2>,
        combine: (T, T2) -> R,
    ) = CombineWithGen(this, nextGen as GenImpl<T2>, combine)

    override fun filter(
        threshold: Int,
        predicate: (T) -> Boolean,
    ) = TODO()

    override fun ignoreExceptions(
        klass: KClass<out Exception>,
        threshold: Int,
    ) = TODO()

    override fun list(size: IntRange) = TODO()

    override fun distinctList(size: IntRange) = TODO()
}

internal data class GenResultV2<T>(
    val value: T,
    val shrinks: Sequence<RandomTree>,
)

internal object GenV2Builders : GenBuilders {

    override fun <T> constant(value: T): Gen<T> {
        return ConstantGen(value)
    }

    override fun int(range: IntRange, shrinkTarget: Int): Gen<Int> {
        return IntGen(range, shrinkTarget)
    }

    override fun long(): Gen<Long> {
        TODO()
    }
}
