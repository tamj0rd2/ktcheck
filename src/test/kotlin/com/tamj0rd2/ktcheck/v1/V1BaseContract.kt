package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract

internal abstract class V1BaseContract : BaseContract, GenFacade by GenV1.Companion
