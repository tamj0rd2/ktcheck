package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.GenerationException.OneOfEmpty

internal class OneOfGenerator<T>(
    private val gens: List<GenV1<T>>,
) : GenV1<T>() {
    init {
        if (gens.isEmpty()) throw OneOfEmpty()
    }

    val indexGen = int(0..<gens.size) as GenV1

    override fun GenContext.generate(): GenResult<T> {
        val (index, indexShrinks) = indexGen.generate(tree.left, mode)
        val (value, valueShrinks) = gens[index].generate(tree.right, mode)

        val shrinks = sequence {
            yieldAll(indexShrinks.map { tree.withLeft(it) })
            yieldAll(valueShrinks.map { tree.withRight(it) })
        }

        return GenResult(value = value, shrinks = shrinks)
    }
}

