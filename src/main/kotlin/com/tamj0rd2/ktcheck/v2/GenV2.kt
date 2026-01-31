package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.CombinerContext
import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.core.RandomTree
import com.tamj0rd2.ktcheck.core.Seed
import kotlin.reflect.KClass

internal sealed class GenV2<T> : Gen<T> {
    abstract fun generate(tree: RandomTree): GenResultV2<T>
    open fun edgeCases(): List<GenResultV2<T>> = emptyList()

    override fun sample(seed: Long): T {
        return generate(RandomTree.new(Seed(seed))).value ?: error("sample(seed) only supported for GenV2")
    }

    override fun <R> map(fn: (T) -> R): GenV2<R> {
        return MapGenV2(this, fn)
    }

    override fun <R> flatMap(fn: (T) -> Gen<R>): GenV2<R> {
        @Suppress("UNCHECKED_CAST")
        return FlatMapGenV2(this, fn as (T) -> GenV2<R>)
    }

    override fun <T2, R> combineWith(
        nextGen: Gen<T2>,
        combine: (T, T2) -> R,
    ): GenV2<R> {
        return CombineGenV2(this, nextGen as GenV2, combine)
    }

    override fun filter(
        threshold: Int,
        predicate: (T) -> Boolean,
    ): GenV2<T> {
        return PredicateFilterGenV2(this, threshold, predicate)
    }

    override fun ignoreExceptions(
        klass: KClass<out Exception>,
        threshold: Int,
    ): GenV2<T> {
        return ExceptionIgnoringGenV2(this, threshold, klass)
    }

    override fun list(
        size: IntRange,
        distinct: Boolean,
    ): GenV2<List<T>> = if (distinct) {
        DistinctListGenV2(this, size)
    } else {
        ListGenV2(this, size)
    }
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

internal object GenV2Builders : GenBuilders {

    override fun <T> combine(block: CombinerContext.() -> T): Gen<T> {
        return CombinerGenV2(block)
    }

    override fun <T> constant(value: T): Gen<T> {
        return ConstantGenV2(value)
    }

    override fun bool(shrinkTarget: Boolean): Gen<Boolean> = int(
        range = 0..1,
        shrinkTarget = if (shrinkTarget) 1 else 0
    ).map { it == 1 }

    override fun int(range: IntRange, shrinkTarget: Int): Gen<Int> {
        return IntGenV2(range, shrinkTarget)
    }

    override fun long(): Gen<Long> {
        // todo: implement this properly
        return int().map { it.toLong() }
    }

    override fun <T> oneOf(gens: Collection<Gen<T>>): Gen<T> {
        val gensList = gens.map { it as GenV2<T> }
        return int(gensList.indices).flatMap { gensList[it] }
    }
}
