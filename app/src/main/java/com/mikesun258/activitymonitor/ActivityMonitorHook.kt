package com.mikesun258.activitymonitor

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityMonitorHook : IXposedHookLoadPackage {
    private val TAG = "ActivityMonitor"

    // 模块被加载时输出日志
    init {
        Log.i(TAG, "✅ 模块已被 LSPosed 加载！")
    }

    // 当 Hook 目标应用时会调用这个方法
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 只输出包名日志，不做任何 Hook
        Log.i(TAG, "📦 正在 Hook 包: ${lpparam.packageName}")
    }
}
