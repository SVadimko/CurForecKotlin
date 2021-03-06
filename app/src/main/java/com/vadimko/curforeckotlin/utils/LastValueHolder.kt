package com.vadimko.curforeckotlin.utils

import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS
import com.vadimko.curforeckotlin.utils.LastValueHolder.lastValueList

/**
 * Holder for actual values of last update currency from Tinkov bank
 * @property lastValueList list of last updated values [CurrencyTCS] from Tinkov bank
 */
object LastValueHolder {
    var lastValueList: List<CurrencyTCS> = listOf(CurrencyTCS(), CurrencyTCS(), CurrencyTCS())
}