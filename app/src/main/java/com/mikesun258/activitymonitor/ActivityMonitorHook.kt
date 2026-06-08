 package com.mikesun258.activitymonitor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"
    private val BROADCAST_ACTION = "com.mikesun258.activitymonitor.EVENT"

    init {
        Log.i(TAG, "✅ 模块已被 LSPosed 加载！")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.i(TAG, "📦 正在 Hook 包: ${lpparam.packageName}")
        try {
            val activityClass = lpparam.classLoader.loadClass("android.app.Activity")

            // 1. Hook onCreate（页面创建时）
            XposedBridge.hookAllMethods(activityClass, "onCreate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.thisObject as Activity
                    sendBroadcast(act, "onCreate")
                }
            })

            // 2. Hook onStart（页面可见时）
            XposedBridge.hookAllMethods(activityClass, "onStart", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.thisObject as Activity
                    sendBroadcast(act, "onStart")
                }
            })

            // 3. Hook onResume（页面可交互时，前台焦点）
            XposedBridge.hookAllMethods(activityClass, "onResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.thisObject as Activity
                    sendBroadcast(act, "onResume")
                }
            })

            // 4. Hook onPause（页面暂停时，失去焦点）
            XposedBridge.hookAllMethods(activityClass, "onPause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.thisObject as Activity
                    sendBroadcast(act, "onPause")
                }
            })

            // 5. Hook onRestart（页面从后台切回前台时）
            XposedBridge.hookAllMethods(activityClass, "onRestart", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val act = param.thisObject as Activity
                    sendBroadcast(act, "onRestart")
                }
            })

        } catch (e: Throwable) {
            Log.e(TAG, "Hook 失败: ${lpparam.packageName}", e)
        }
    }

    private fun sendBroadcast(activity: Activity, type: String) {
        val pkgName = activity.packageName
        val actName = activity.javaClass.name
        Log.d(TAG, "$type | $pkgName / $actName")

        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("pkg_name", pkgName)
            putExtra("act_name", actName)
            putExtra("event_type", type)
            setPackage(pkgName)
        }
        activity.sendBroadcast(intent)
    }
}
