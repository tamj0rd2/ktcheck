package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.Seed
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow
import kotlin.reflect.KClass

// todo: one thing on my mind is that each generator should do a final check to make sure the constraints are upheld
//  post generation/edge case creation. put that in a contract somewhere.
internal sealed interface GenImpl<T> : Gen<T> {
    fun generate(root: RandomTree): Result4k<GeneratedValue<T>, GenerationException>

    fun edgeCases(root: RandomTree): List<GeneratedValue<T>>

    override fun withoutDefaultEdgeCases() = EdgeCasesDisabledGen(this)

    override fun sample(seed: Long) = generate(RandomTree.new(Seed(seed))).orThrow().value

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
    ) = FilterGen(this, threshold, predicate)

    override fun ignoreExceptions(
        klass: KClass<out Exception>,
        threshold: Int,
    ) = IgnoreExceptionGen(this, klass, threshold)

    override fun list(size: IntRange) = ListGen(this, size)

    override fun distinctList(size: IntRange) = DistinctListGen(this, size)
}

internal data class GeneratedValue<T>(
    val value: T,
    val shrinks: Sequence<RandomTree>,
    val usedTree: RandomTree,
)

internal object GenV2Builders : GenBuilders {

    override fun <T> constant(value: T) = ConstantGen(value)

    override fun int(range: IntRange, shrinkTarget: Int) = IntGen(range, shrinkTarget)

    override fun long(): Gen<Long> {
        // todo: implement properly
        return int().map { it.toLong() }
    }
}
