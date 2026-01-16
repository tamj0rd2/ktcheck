package com.tamj0rd2.ktcheck

sealed class GenerationException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause) {
    @Suppress("unused")
    class OneOfEmpty internal constructor() : GenerationException("Gen.oneOf() called with no generators")

    class FilterLimitReached internal constructor(threshold: Int, cause: Throwable? = null) :
        GenerationException("Filter failed after $threshold misses", cause)

    class DistinctCollectionSizeImpossible internal constructor(targetSize: Int, achievedSize: Int, attempts: Int) :
        GenerationException(
            "Failed to generate a list of size $targetSize with distinct elements after $attempts attempts. Only achieved size $achievedSize."
        )
}
