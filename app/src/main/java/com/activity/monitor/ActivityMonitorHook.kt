package com.mikesun258.activitymonitor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"
    private val BROADCAST_ACTION = "com.mikesun258.activitymonitor.EVENT"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 1. Hook Activity.onCreate
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        Log.d(TAG, "OnCreate | $pkg / $cls")
                        sendEventBroadcast(act, pkg, cls, "onCreate")
                    }
                }
            )

            // 2. Hook Activity.onResume
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                lpparam.classLoader,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        Log.d(TAG, "OnResume | $pkg / $cls")
                        sendEventBroadcast(act, pkg, cls, "onResume")
                    }
                }
            )

            // 3. Hook Activity.onPause
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                lpparam.classLoader,
                "onPause",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        Log.d(TAG, "OnPause | $pkg / $cls")
                        sendEventBroadcast(act, pkg, cls, "onPause")
                    }
                }
            )

        } catch (e: Throwable) {
            Log.e(TAG, "Hook failed for package: ${lpparam.packageName}", e)
        }
    }

    private fun sendEventBroadcast(activity: Activity, pkg: String, cls: String, type: String) {
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("pkg_name", pkg)
            putExtra("act_name", cls)
            putExtra("event_type", type)
            setPackage(activity.packageName)
        }
        activity.sendBroadcast(intent)
    }
}
