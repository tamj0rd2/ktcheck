package com.tamj0rd2.ktcheck.gen

import com.tamj0rd2.ktcheck.producer.ProducerTree

private class BooleanGenerator : Gen<Boolean>() {
    override fun generate(tree: ProducerTree): GenResult<Boolean> {
        val value = tree.producer.bool()
        return GenResult(
            value = value,
            shrinks = if (value) sequenceOf(tree.withValue(false)) else emptySequence()
        )
    }
}

fun Gen.Companion.bool(): Gen<Boolean> = BooleanGenerator()
