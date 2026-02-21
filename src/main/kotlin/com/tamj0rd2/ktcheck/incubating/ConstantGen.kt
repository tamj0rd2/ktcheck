package com.tamj0rd2.ktcheck.incubating

internal class ConstantGen<T>(
    private val value: T,
) : GenImpl<T>() {
    override fun generate(tree: RandomTree): GenResultV2<T> =
        GenResultV2(value = value, tree = tree, shrinks = emptySequence())

    override fun edgeCases(tree: RandomTree): List<GenResultV2<T>> {
        return emptyList()
    }
}
