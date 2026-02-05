package com.tamj0rd2.ktcheck

/**
 * Marks an API as experimental, meaning that it is not yet stable and may be removed or changed in future releases.
 * Use with caution and be prepared for potential breaking changes when using APIs annotated with @Experimental.
 */
@MustBeDocumented
@RequiresOptIn(
    message = "This is an experimental feature and may be removed or changed in future releases. Use with caution",
    level = RequiresOptIn.Level.WARNING
)
annotation class ExperimentalKtCheck
