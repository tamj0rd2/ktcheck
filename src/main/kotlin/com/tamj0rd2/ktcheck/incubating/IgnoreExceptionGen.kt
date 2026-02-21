package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException
import kotlin.reflect.KClass

internal class IgnoreExceptionGen<T>(
    private val wrappedGen: GenImpl<T>,
    private val klass: KClass<out Exception>,
    private val threshold: Int,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GenResultV2<T> {
        var latestError: Exception? = null

        return generateSequence(root) { it.right }
            .take(threshold)
            .mapNotNull {
                try {
                    wrappedGen.generate(it.left)
                } catch (e: Exception) {
                    if (!klass.isInstance(e)) throw e
                    latestError = e
                    null
                }
            }
            .firstOrNull() ?: throw GenerationException.FilterLimitReached(threshold, latestError)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<T>> {
        return emptyList()
    }
}
