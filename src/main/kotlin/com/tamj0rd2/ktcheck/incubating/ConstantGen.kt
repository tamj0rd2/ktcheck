package com.tamj0rd2.ktcheck.incubating

internal class ConstantGen<T>(
    private val value: T,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GenResultV2<T> =
        GenResultV2(value = value, tree = root, shrinks = emptySequence())

    override fun edgeCases(root: RandomTree): List<GenResultV2<T>> {
        return emptyList()
    }
}
