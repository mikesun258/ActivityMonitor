package com.mikesun258.activitymonitor

import android.app.Activity
import android.content.Intent
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"
    private val BROADCAST_ACTION = "com.mikesun258.activitymonitor.EVENT"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Log.i(TAG, "📦 Hook: ${lpparam.packageName}")
        try {
            val activityClass = lpparam.classLoader.loadClass("android.app.Activity")

            // 监听所有关键生命周期
            listOf("onCreate", "onStart", "onResume", "onPause", "onRestart").forEach { method ->
                XposedBridge.hookAllMethods(activityClass, method, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        sendBroadcast(act, method)
                    }
                })
            }

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
