package com.xj.hookdemo.rxjava

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.xj.hookdemo.R
import de.robv.android.xposed.DexposedBridge
import de.robv.android.xposed.XC_MethodHook
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.internal.operators.observable.ObservableFromCallable
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.Callable

class RxJavaHookActivity : AppCompatActivity() {

    private val TAG = "RxJavaHookActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_java_hook)
        hookRxFromCallable()
        Rx2Utils.setRxOnErrorHandler()

        findViewById<View>(R.id.btn_rx_task).setOnClickListener {

            backgroundTask(Callable<Any> {
                Log.i(TAG, "btn_rx_task: ")
                Thread.sleep(30)
                return@Callable null
            })?.subscribe()

        }

        findViewById<View>(R.id.btn_rx_task_safe).setOnClickListener {
            try {
                backgroundTask(Callable<Any> {
                    Log.i(TAG, "btn_rx_task: ")
                    Thread.sleep(30)
                    return@Callable null
                })?.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onCreate: e is $e")
            }

        }
    }

    /**
     * 创建一个rx的子线程任务Observable
     */
    private fun <T> backgroundTask(callable: Callable<T>?): Observable<T>? {
        return Observable.fromCallable(callable)
                .compose(IOMain())
    }

    fun hookRxFromCallable() {
//        DexposedBridge.findAndHookMethod(ObservableFromCallable::class.java, "subscribeActual", Observer::class.java, RxMethodHook())
        DexposedBridge.findAndHookMethod(Observable::class.java, "fromCallable", Callable::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                super.beforeHookedMethod(param)
                val args = param?.args
                args ?: return
                val javaClass = args[0].javaClass
                Log.i(TAG, "beforeHookedMethod: javaClass is $javaClass")
                val callable = args[0] as Callable<*>
                if (callable.call() == null) {
                    val buildStackTrace = Rx2Utils.buildStackTrace()
                    Log.e(TAG, "beforeHookedMethod: buildStackTrace is $buildStackTrace")
                }
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                super.afterHookedMethod(param)
            }
        })
    }

    internal class RxMethodHook : XC_MethodHook() {
        @Throws(Throwable::class)
        override fun beforeHookedMethod(param: MethodHookParam) {
            super.beforeHookedMethod(param)
            val buildStackTrace = Rx2Utils.buildStackTrace()
            val observableFromCallable = param.thisObject as ObservableFromCallable<*>

            val callableFiled = observableFromCallable.javaClass.getDeclaredField("callable")
            callableFiled.isAccessible = true
            val callable = callableFiled.get(observableFromCallable) as Callable<*>
            val call = callable.call()
            if (call == null) {
                Log.e(TAG, Log.getStackTraceString(Throwable()))
                Log.e(TAG, "error, callable#call should not return null, buildStackTrace is $buildStackTrace")
            }


            Log.i(TAG, "beforeHookedMethod declaredField:${callable}")
            Log.i(TAG, "beforeHookedMethod method:${param.method}")
            Log.i(TAG, "beforeHookedMethod thisObject:${param.thisObject}")
            Log.i(TAG, "beforeHookedMethod args:${param.args}")
            Log.i(TAG, "beforeHookedMethod result:${param.result}")
            Log.i(TAG, "beforeHookedMethod throwable:${param.throwable}")
        }

        @Throws(Throwable::class)
        override fun afterHookedMethod(param: MethodHookParam) {
            super.afterHookedMethod(param)
            val observableFromCallable = param.thisObject as ObservableFromCallable<*>

            Log.i(TAG, "afterHookedMethod method:${param.method}")
            Log.i(TAG, "afterHookedMethod thisObject:${param.thisObject}")
            Log.i(TAG, "afterHookedMethod args:${param.args}")
            Log.i(TAG, "afterHookedMethod result:${param.result}")
            Log.i(TAG, "afterHookedMethod throwable:${param.throwable}")
        }

        companion object {
            private const val TAG = "RxJavaHookActivity"
        }
    }

}