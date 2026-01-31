package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.CombinerContext
import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.core.RandomTree
import kotlin.reflect.KClass

internal interface GenV2<T> : Gen<T> {
    fun generate(tree: RandomTree): GenResultV2<T>

    companion object : GenFacade by GenV2Facade
}

internal data class GenResultV2<T>(
    val value: T,
    val shrinks: Sequence<GenResultV2<T>>,
) {
    fun <R> map(fn: (T) -> R): GenResultV2<R> =
        GenResultV2(
            value = fn(value),
            shrinks = shrinks.map { it.map(fn) }
        )

    fun filter(predicate: (T) -> Boolean): GenResultV2<T>? {
        if (!predicate(value)) return null

        return GenResultV2(
            value = value,
            shrinks = shrinks.mapNotNull { it.filter(predicate) }
        )
    }
}

private object GenV2Facade : GenFacade {
    override fun <T> Gen<T>.sample(seed: Long): T {
        return (this as? GenV2<T>)?.generate(
            RandomTree.new(
                com.tamj0rd2.ktcheck.core.Seed(
                    seed
                )
            )
        )?.value
            ?: error("sample(seed) only supported for GenV2")
    }

    override fun <T, R> Gen<T>.map(fn: (T) -> R): Gen<R> {
        return MapGenV2(this as GenV2, fn)
    }

    override fun <T, R> Gen<T>.flatMap(fn: (T) -> Gen<R>): Gen<R> {
        @Suppress("UNCHECKED_CAST")
        return FlatMapGenV2(this as GenV2, fn as (T) -> GenV2<R>)
    }

    override fun <T1, T2, R> Gen<T1>.combineWith(
        nextGen: Gen<T2>,
        combine: (T1, T2) -> R,
    ): Gen<R> {
        return CombineGenV2(this as GenV2, nextGen as GenV2, combine)
    }

    override fun <T> combine(block: CombinerContext.() -> T): Gen<T> {
        return CombinerGenV2(block)
    }

    override fun <T> Gen<T>.filter(
        threshold: Int,
        predicate: (T) -> Boolean,
    ): Gen<T> {
        return PredicateFilterGenV2(this as GenV2, threshold, predicate)
    }

    override fun <T> Gen<T>.ignoreExceptions(
        klass: KClass<out Exception>,
        threshold: Int,
    ): Gen<T> {
        return ExceptionIgnoringGenV2(this as GenV2, threshold, klass)
    }

    override fun <T> constant(value: T): Gen<T> {
        return ConstantGenV2(value)
    }

    override fun bool(origin: Boolean): Gen<Boolean> {
        return BoolGenV2(origin)
    }

    override fun int(range: IntRange): Gen<Int> {
        return IntGenV2(range)
    }

    override fun long(): Gen<Long> {
        // todo: implement this properly
        return int().map { it.toLong() }
    }

    override fun <T> Gen<T>.list(
        size: IntRange,
        distinct: Boolean,
    ): Gen<List<T>> = if (distinct) {
        DistinctListGenV2(this as GenV2, size)
    } else {
        ListGenV2(this as GenV2, IntGenV2(size))
    }

    override fun <T> oneOf(gens: Collection<Gen<T>>): Gen<T> {
        val gensList = gens.map { it as GenV2<T> }
        return int(gensList.indices).flatMap { gensList[it] }
    }
}
