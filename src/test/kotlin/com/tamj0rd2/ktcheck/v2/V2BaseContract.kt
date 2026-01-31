package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract

internal abstract class V2BaseContract : BaseContract, GenBuilders by GenV2Builders
