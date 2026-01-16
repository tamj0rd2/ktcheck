package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree

internal class ConstantGenV2<T>(private val value: T) : GenV2<T> {
    override fun generate(tree: ProducerTree): GenResultV2<T> {
        return GenResultV2(value, emptySequence())
    }
}
