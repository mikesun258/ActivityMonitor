package com.mikesun258.activitymonitor

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"

    // 每个状态对应一个不同的 Broadcast Action
    private val BROADCAST_ACTION_CREATE = "com.mikesun258.activitymonitor.EVENT1"  // onCreate
    private val BROADCAST_ACTION_START  = "com.mikesun258.activitymonitor.EVENT2"  // onStart
    private val BROADCAST_ACTION_RESUME = "com.mikesun258.activitymonitor.EVENT3"  // onResume
    private val BROADCAST_ACTION_PAUSE  = "com.mikesun258.activitymonitor.EVENT4"  // onPause
    private val BROADCAST_ACTION_STOP   = "com.mikesun258.activitymonitor.EVENT5"  // onStop

    init {
        Log.i(TAG, "✅ 模块已被 LSPosed 加载！")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.i(TAG, "📦 正在 Hook 包: ${lpparam.packageName}")
        try {
            val instrumentationClass = lpparam.classLoader.loadClass("android.app.Instrumentation")

            // 1. onCreate → EVENT1
            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTION_CREATE, "onCreate")
                }
            })

            // 2. onStart → EVENT2
            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnStart", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTION_START, "onStart")
                }
            })

            // 3. onResume → EVENT3
            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTION_RESUME, "onResume")
                }
            })

            // 4. onPause → EVENT4
            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnPause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTION_PAUSE, "onPause")
                }
            })

            // 5. onStop → EVENT5
            XposedBridge.hookAllMethods(instrumentationClass, "callActivityOnStop", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.args[0] as Activity
                    sendBroadcast(act, BROADCAST_ACTION_STOP, "onStop")
                }
            })

        } catch (e: Throwable) {
            Log.e(TAG, "Hook 失败: ${lpparam.packageName}", e)
        }
    }

    private fun sendBroadcast(activity: Activity, action: String, type: String) {
        val pkgName = activity.packageName
        val actName = activity.javaClass.name
        Log.d(TAG, "$type ($action) | $pkgName / $actName")

        val intent = Intent(action).apply {
            putExtra("pkg_name", pkgName)
            putExtra("act_name", actName)
            putExtra("event_type", type)
        }
        activity.sendBroadcast(intent)
    }
}
