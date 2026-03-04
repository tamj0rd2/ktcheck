package com.tamj0rd2.ktcheck.current

internal class EdgeCasesDisabledGen<T>(
    private val delegate: GenImpl<T>,
) : GenImpl<T> by delegate {
    override fun edgeCases(root: RandomTree): List<GeneratedValue<T>> {
        return emptyList()
    }
}
