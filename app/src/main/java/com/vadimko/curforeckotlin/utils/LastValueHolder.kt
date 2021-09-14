package com.vadimko.curforeckotlin.utils

import com.vadimko.curforeckotlin.tcsApi.CurrencyTCS

/**
 * Holder for actual values of last update currency from Tinkov bank
 * @property lastValueList list of last updated values [CurrencyTCS] from Tinkov bank
 */
object LastValueHolder {
    lateinit var lastValueList: List<CurrencyTCS>
}