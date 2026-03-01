package com.tamj0rd2.ktcheck.incubating

internal class ConstantGen<T>(
    private val value: T,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GeneratedValue<T> =
        GeneratedValue(value = value, shrinks = emptySequence(), usedTree = root)

    override fun edgeCases(root: RandomTree): List<GeneratedValue<T>> {
        return emptyList()
    }
}
