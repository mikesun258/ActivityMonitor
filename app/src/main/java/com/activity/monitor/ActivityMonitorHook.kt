package com.activity.monitor

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
    private val BROADCAST_ACTION = "com.activity.monitor.EVENT"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        val msg = "OnCreate | $pkg / $cls"
                        Log.d(TAG, msg)
                        sendEventBroadcast(act, pkg, cls, "onCreate")
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        val msg = "OnResume | $pkg / $cls"
                        Log.d(TAG, msg)
                        sendEventBroadcast(act, pkg, cls, "onResume")
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                Activity::class.java,
                "onPause",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val act = param.thisObject as Activity
                        val pkg = act.packageName
                        val cls = act.javaClass.name
                        val msg = "OnPause  | $pkg / $cls"
                        Log.d(TAG, msg)
                        sendEventBroadcast(act, pkg, cls, "onPause")
                    }
                }
            )

        } catch (e: Throwable) {
            Log.e(TAG, "Hook failed: ${lpparam.packageName}", e)
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

