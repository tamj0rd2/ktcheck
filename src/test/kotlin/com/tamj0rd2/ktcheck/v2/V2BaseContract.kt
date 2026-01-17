package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract

internal abstract class V2BaseContract : BaseContract, GenFacade by GenV2.Companion
