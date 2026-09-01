package com.aura.core.common.util

import timber.log.Timber

object Logger {
    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Timber.tag(tag).e(throwable, message)
    }

    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }
}
