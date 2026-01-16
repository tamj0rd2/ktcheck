package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class V2BaseContract : BaseContract, GenFacade by GenV2.Companion {
    override fun <T> Gen<T>.generate(tree: ProducerTree): T {
        return (this as GenV2).generate(tree).value
    }

    override fun <T> Gen<T>.generateWithShrunkValues(tree: ProducerTree): Pair<T, List<T>> {
        val (value, shrinks) = (this as GenV2).generate(tree)
        return value to shrinks.map { it.value }.toList()
    }
}
