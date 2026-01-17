package com.tamj0rd2.ktcheck

sealed class GenerationException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause) {
    @Suppress("unused")
    class OneOfEmpty internal constructor() : GenerationException("Gen.oneOf() called with no generators")

    class FilterLimitReached internal constructor(threshold: Int, cause: Throwable? = null) :
        GenerationException("Filter failed after $threshold misses", cause)

    class DistinctCollectionSizeImpossible internal constructor(minSize: Int, achievedSize: Int, attempts: Int) :
        GenerationException(
            "Failed to generate a list of size $minSize with distinct elements after $attempts attempts. Only achieved size $achievedSize."
        )

    class ConditionalLogicDetectedDuringCombine internal constructor(
        originalBindCount: Int,
        bindCountOnRerun: Int,
    ) : GenerationException(
        """
        |Conditional logic detected inside combine().
        |Original generation called bind $originalBindCount times, but shrinking called bind $bindCountOnRerun times.
        |The use of conditionals that affect the order or presence of bind() calls within combine() is prohibited
        |because it can lead to invalid and non-deterministic generation/shrinking.
        |Check the docs for combine() for more details.
        """.trimMargin()
    )
}
