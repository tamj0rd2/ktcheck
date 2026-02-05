package com.tamj0rd2.ktcheck.current

internal class ConstantGen<T>(private val value: T) : GenImpl<T>() {
    override fun generate(tree: RandomTree): GenResultV2<T> {
        return GenResultV2(value, emptySequence())
    }
}
