package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.contracts.ShrinkingChallengeContract
import org.junit.jupiter.api.Disabled

// based on https://github.com/jlink/shrinking-challenge/tree/main/challenges
@Disabled
internal class V2ShrinkingChallengeTest : V2BaseContract(), ShrinkingChallengeContract
