package com.xj.hookdemo.rxjava

import android.util.Log
import hu.akarnokd.rxjava2.debug.RxJavaAssemblyException
import io.reactivex.plugins.RxJavaPlugins


object Rx2Utils {

    private const val TAG = "Rx2Utils"

    /**
     * 设置全局的 onErrorHandler。
     */
    fun setRxOnErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            val assembled = RxJavaAssemblyException.find(throwable)
            if (assembled != null) {
                Log.e(TAG, assembled.stacktrace())
            } else {
                throwable.printStackTrace()
            }
        }
    }

    fun buildStackTrace(): String? {
        val b = StringBuilder()
        val es = Thread.currentThread().stackTrace
        b.append("RxJavaAssemblyException: assembled\r\n")
        for (e in es) {
            if (filter(e)) {
                b.append("at ").append(e).append("\r\n")
            }
        }
        return b.toString()
    }

    /**
     * Filters out irrelevant stacktrace entries.
     * @param e the stacktrace element
     * @return true if the element may pass
     */
    private fun filter(e: StackTraceElement): Boolean {
        // ignore bridge methods
        if (e.lineNumber == 1) {
            return false
        }
        val cn = e.className
        if (cn.contains("java.lang.Thread")) {
            return false
        }

        // ignore JUnit elements
        if (cn.contains("junit.runner")
                || cn.contains("org.junit.internal")
                || cn.contains("junit4.runner")) {
            return false
        }

        // ignore reflective accessors
        if (cn.contains("java.lang.reflect")
                || cn.contains("sun.reflect")) {
            return false
        }

        // ignore RxJavaAssemblyException itself
        if (cn.contains(".RxJavaAssemblyException")) {
            return false
        }

        // the shims injecting the error
        return if (cn.contains("OnAssembly")
                || cn.contains("RxJavaAssemblyTracking")
                || cn.contains("RxJavaPlugins")) {
            false
        } else true
    }

}