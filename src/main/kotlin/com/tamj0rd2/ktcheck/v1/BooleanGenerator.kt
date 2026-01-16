package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.core.shrinkers.BoolShrinker

internal class BooleanGenerator(
    private val origin: Boolean,
) : GenV1<Boolean>() {
    override fun GenContext.generate(): GenResult<Boolean> {
        val value = tree.producer.bool()
        return GenResult(
            value = value,
            shrinks = BoolShrinker.shrink(value = value, origin = origin).map(tree::withValue)
        )
    }
}
