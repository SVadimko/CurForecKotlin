package com.vadimko.curforeckotlin.utils

import android.util.Log
import com.vadimko.curforeckotlin.utils.ScopeCreator.handler
import com.vadimko.curforeckotlin.utils.ScopeCreator.scope
import kotlinx.coroutines.*

/**
 * Util class to create custom coroutine scope
 * @property [handler] CoroutineExeptionHandler for avoiding to cancel scope if one of the coroutines
 * produce Exception
 * @property [scope] instance of custom [CoroutineScope]
 */
object ScopeCreator {

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.wtf("Coroutine exception", "$exception")
    }
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)

    fun createScope() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }

    /**
     * @return instance of custom CoroutineScope
     */
    fun getScope(): CoroutineScope {
        return scope
    }

    /**
     * Cancel scope and all coroutines launched with it
     */
    fun cancelScope() {
        scope.cancel()
    }
}